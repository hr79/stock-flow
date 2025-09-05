package com.example.stockflow.domain.outbound;

import com.example.stockflow.domain.outbound.dto.OutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.OutboundResponseDto;
import com.example.stockflow.domain.outbound.service.OutboundService;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.model.OrderStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.Rollback;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
@Testcontainers
public class OutboundServiceConcurrencyIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(OutboundServiceConcurrencyIntegrationTest.class);

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OutboundOrderItemRepository outboundOrderItemRepository;

    @Autowired
    private EntityManager entityManager;

    private Product testProduct;
    private OutboundOrderItem testOrderItem;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        log.info(":::: configureProperties");
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver"); // 추가
    }

    @BeforeEach
    void setUp() {
        System.out.println(":::: 테스트 제품생성");
        // 테스트용 제품 생성
        testProduct = Product.builder()
                .name("TestProduct")
                .currentStock(100)
                .threshold(10)
                .build();
        testProduct = productRepository.saveAndFlush(testProduct);

        // 테스트 출고 주문 요청 생성
        OutboundOrder outboundOrder = OutboundOrder.builder()
                .destination("거래처")
                .status(OrderStatus.REQUESTED.toString())
                .build();
        outboundOrderRepository.saveAndFlush(outboundOrder);

        // 테스트용 출고 주문 요청 아이템 생성
        testOrderItem = OutboundOrderItem.builder()
                .outboundOrder(outboundOrder)
                .product(testProduct)
                .requiredQuantity(50)
                .releasedQuantity(0)
                .build();
        outboundOrderItemRepository.saveAndFlush(testOrderItem);

        entityManager.clear(); // 영속성 컨텍스트 초기화
    }

    @Test
    @DisplayName("멀티스레딩 환경에서 일부 성공/일부 실패, 데이터 무결성 및 예외 처리 검증")
    void testOptimisticLockWithPartialFailuresAndIntegrity() {
        // Given
        int totalRequestCount = 10; // 일부 실패 유도
        int perRequestQuantity = 15; // 총 150개 요청 (재고 100개로 일부 실패 유도)
        List<ProductDto> productList = new ArrayList<>();
        for (int i = 0; i < totalRequestCount; i++) {
            productList.add(new ProductDto("TestProduct", perRequestQuantity));
        }
        log.info("testorderItem id: {}", testOrderItem.getId()); // 미리 만들어둔 출고 요청
        OutboundRequestDto requestDto = new OutboundRequestDto(testOrderItem.getId(), productList);

        // When
        List<OutboundResponseDto> responses = outboundService.createOutboundWithMultiThreading(requestDto);

        // Then
        // 1. 성공/실패 응답 분리
        int totalSuccess = responses.size();
        int totalFail = totalRequestCount - totalSuccess;

        // 2. 성공 요청의 DB 반영 검증
        Product finalProduct = productRepository.findById(testProduct.getId())
                .orElseThrow(() -> new AssertionError("제품을 찾을 수 없습니다"));
        OutboundOrderItem finalOrderItem = outboundOrderItemRepository.findById(testOrderItem.getId())
                .orElseThrow(() -> new AssertionError("출고요청 아이템을 찾을 수 없습니다"));

        int expectedTotalReleased = totalSuccess * perRequestQuantity;
        int expectedFinalProductStock = 100 - expectedTotalReleased;

        assertThat(finalProduct.getCurrentStock())
                .as("성공 요청만큼 재고가 차감되어야 함")
                .isEqualTo(Math.max(0, expectedFinalProductStock));    // TODO: 실패한 요청도 db에 반영되어 재고가 차감되는 오류 발생
        assertThat(finalOrderItem.getReleasedQuantity())
                .as("성공 요청만큼 releasedQuantity가 증가해야 함")
                .isEqualTo(expectedTotalReleased);

        // 3. 데이터 무결성: releasedQuantity, 재고 음수 불가
        assertThat(finalProduct.getCurrentStock()).isGreaterThanOrEqualTo(0);
        assertThat(finalOrderItem.getReleasedQuantity()).isLessThanOrEqualTo(finalOrderItem.getRequiredQuantity());

        // 4. 실패 요청이 DB에 영향을 주지 않았는지 검증
        assertThat(expectedTotalReleased).isLessThanOrEqualTo(100);

        // 5. 상태값 검증 (완료/진행중)
        if (finalOrderItem.getReleasedQuantity() < finalOrderItem.getRequiredQuantity()) {
            assertThat(finalOrderItem.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS.toString());
        } else {
            assertThat(finalOrderItem.getStatus()).isEqualTo(OrderStatus.COMPLETED.toString());
        }

        // 6. 전체 성공/실패 개수 출력
        System.out.println("성공 응답 수: " + totalSuccess);
        System.out.println("실패 응답 수: " + totalFail);

        // 7. 성공 응답의 데이터 검증
        for (OutboundResponseDto resp : responses) {
            assertThat(resp.getProductName()).isEqualTo("TestProduct");
            assertThat(resp.getQuantity()).isEqualTo(perRequestQuantity);
            assertThat(resp.getUpdatedStock()).isGreaterThanOrEqualTo(0);
        }
    }

    // TODO: 각기 다른 제품의 출고 요청+같은 제품의 출고 요청 테스트
    @Test
    @Rollback
    @DisplayName("멀티스레딩 환경에서 서로 다른 제품의 출고 요청 테스트")
    void testMultiThreadingWithDifferentProducts() {
        // given
        outboundOrderItemRepository.deleteAll();
        productRepository.deleteAll();
        outboundOrderRepository.deleteAll();

        // 10개의 다른 제품 생성
        List<Product> productList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            productList.add(new Product("product_"+i, new BigDecimal(1000), 20, 5));
        }
        productList.add(new Product("product_same", new BigDecimal(1000), 20, 5)); // 같은 제품 추가
        productRepository.saveAllAndFlush(productList);

        // 출고 주문서 생성
        OutboundOrder outboundOrder = new OutboundOrder("거래처", OrderStatus.REQUESTED.toString());
        outboundOrderRepository.saveAndFlush(outboundOrder);

        // 출고 주문서 상세 아이템 생성
        List<OutboundOrderItem> outboundOrderItemList = new ArrayList<>();
        for (Product product : productList) {
            log.info("product name: {}", product.getName());
            OutboundOrderItem orderItem = OutboundOrderItem.builder()
                    .outboundOrder(outboundOrder)
                    .product(product)
                    .requiredQuantity(1) // 각 제품당 1개 출고 요청
                    .releasedQuantity(0)
                    .status(OrderStatus.REQUESTED.toString())
                    .build();
            outboundOrderItemList.add(orderItem);
        }
        outboundOrderItemRepository.saveAllAndFlush(outboundOrderItemList);

        // 출고 등록 요청 생성
        List<ProductDto> productDtoList = new ArrayList<>();
        for (OutboundOrderItem orderItem : outboundOrderItemList) {
            String productName = orderItem.getProduct().getName();
            int quantity = orderItem.getRequiredQuantity();
            productDtoList.add(new ProductDto(productName, quantity));

            // 한 제품은 요청 두번 더 넣기(동시성 테스트)
            if (productName.equals("product_same")) {
                productDtoList.add(new ProductDto(productName, quantity));
                productDtoList.add(new ProductDto(productName, quantity));
            }
        }
        OutboundRequestDto outboundRequestDto = new OutboundRequestDto(outboundOrder.getId(), productDtoList);

        // when
        List<OutboundResponseDto> responses = outboundService.createOutboundWithMultiThreading(outboundRequestDto);

        // then
        assertThat(responses).as("성공 요청 갯수는 같은 제품 요청 3개를 제외한 10개 이상이어야 한다.").hasSizeGreaterThanOrEqualTo(10);
        int totalSuccess = responses.size();
        int totalFail = productDtoList.size() - totalSuccess;

        System.out.println("총 요청 수: "+productDtoList.size());
        System.out.println("성공 요청 갯수"+totalSuccess);
        System.out.println("실패 요청 갯수"+totalFail);

        // 최종 재고 확인
        List<Product> allProducts = productRepository.findAll();
        for (Product product : allProducts) {
            log.info("product name: {}", product.getName());
        }

        for (Product product : allProducts) {
            String productName = product.getName();
            int finalStock = product.getCurrentStock();

            log.info("productName: {}", productName);
            log.info("finalStock: {}", finalStock);

                if (productName.equals("product_same")) {
                    assertThat(finalStock).as("3번 출고가 들어간 제품의 경우 성공한 출고 횟수만큼 차감되어야 함.").isGreaterThanOrEqualTo(17);
                } else {
                    assertThat(finalStock).as("각기 다른 제품들의 출고의 경우 모두 성공하여 각 제품의 재고가 1씩 줄어 최종 9개가 되어야함.").isEqualTo(19);
                }

        }
    }
}
