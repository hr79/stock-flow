package com.example.stockflow.domain.outbound;

import com.example.stockflow.domain.outbound.dto.OutboundRequestDto;
import com.example.stockflow.domain.outbound.service.OutboundService;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StopWatch;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class RegisterOutboundTest {
    private static final Logger log = LoggerFactory.getLogger(RegisterOutboundTest.class);

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired private OutboundOrderItemRepository outboundOrderItemRepository;

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
        System.out.println(":::: 테스트 데이터 준비 중...");
        List<Product> products = new ArrayList<>();
        OutboundOrder outboundOrder = new OutboundOrder("거래처", OrderStatus.REQUESTED.toString());
        List<OutboundOrderItem> outboundOrderItemList = new ArrayList<>();

        for (int i = 0; i < 10000; i++) {

            Product product = Product.builder()
                    .name("product_" + i)
                    .price(new BigDecimal(1000))
                    .currentStock(100000)
                    .threshold(100)
                    .build();

            products.add(product);

            OutboundOrderItem orderItem = OutboundOrderItem.builder().
                    outboundOrder(outboundOrder)
                    .product(product)
                    .requiredQuantity(1)
                    .status(OrderStatus.REQUESTED.toString())
                    .build();

            outboundOrderItemList.add(orderItem);
        }
        productRepository.saveAll(products);
        outboundOrderRepository.save(outboundOrder);
        outboundOrderItemRepository.saveAll(outboundOrderItemList);
    }

    private OutboundRequestDto createTestRequestDto() {
        List<ProductDto> productDtos = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            ProductDto productDto = new ProductDto("product_" + i, 1);
            productDtos.add(productDto);
        }

        return new OutboundRequestDto(1L, productDtos);
    }

    @Test
    @DisplayName("싱글스레드 vs 멀티스레드 처리 테스트")
    void compareRegisterOutboundWithAndWithoutMultiThreading() {
        StopWatch stopWatch = new StopWatch();

        OutboundRequestDto requestDto = createTestRequestDto();

        // ✅ 싱글스레드 실행
        stopWatch.start("싱글스레드 처리");
        outboundService.createOutbound(requestDto); // 기존 순차 처리 메서드
        stopWatch.stop();

        // ✅ DB 재초기화 (or 별도 데이터로 멀티스레드 테스트)
        resetStock(); // 재고 초기화 함수 추가

        // ✅ 멀티스레드 실행
        stopWatch.start("멀티스레드 처리");
        outboundService.createOutboundWithMultiThreading(requestDto); // 병렬 처리 메서드
        stopWatch.stop();

        System.out.println("▶ 처리 시간 비교");
        System.out.println(stopWatch.prettyPrint());

        assertThat(true).isTrue(); // 성능 측정용, 결과는 로그로 확인
    }

    private void resetStock() {
        productRepository.findAll().forEach(p -> p.setCurrentStock(100000));
    }
}