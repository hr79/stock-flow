package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.*;
import com.example.stockflow.domain.outbound.dto.OutboundOrderRequestDto;
import com.example.stockflow.domain.outbound.dto.OutboundOrderResponseDto;
import com.example.stockflow.domain.outbound.dto.OutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.OutboundResponseDto;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.model.OrderStatus;
import com.example.stockflow.notification.Notifier;
import com.example.stockflow.domain.product.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class OutboundService {
    private final ProductRepository productRepository;
    private final OutboundOrderRepository outboundOrderRepository;
    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final OutboundRepository outboundRepository;
    private final OutboundOrderMapper mapper;
    private final Notifier notifier;
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private final OutboundProcessorService outboundProcessorService;

    public OutboundService(ProductRepository productRepository, OutboundOrderRepository outboundOrderRepository, OutboundOrderItemRepository outboundOrderItemRepository, OutboundRepository outboundRepository, OutboundOrderMapper mapper, @Qualifier("discordNotifier") Notifier notifier, OutboundProcessorService outboundProcessorService) {
        this.productRepository = productRepository;
        this.outboundOrderRepository = outboundOrderRepository;
        this.outboundOrderItemRepository = outboundOrderItemRepository;
        this.outboundRepository = outboundRepository;
        this.mapper = mapper;
        this.notifier = notifier;
        this.outboundProcessorService = outboundProcessorService;
    }

    // 출고 요청
    @Transactional
    public OutboundOrderResponseDto createOutboundOrder(OutboundOrderRequestDto outboundOrderRequestDto) {
        String destination = outboundOrderRequestDto.getDestination();
        OutboundOrder outboundOrder = OutboundOrder.builder().destination(destination).build();
        outboundOrderRepository.save(outboundOrder);

        List<OutboundOrderItem> orderItemList = new ArrayList<>();

        for (ProductDto productDto : outboundOrderRequestDto.getProducts()) {
            String productName = productDto.getProduct();
            Product product = productRepository.findByName(productName).orElseThrow(() -> new IllegalArgumentException("not found product : " + productName));
            int quantity = productDto.getQuantity();

            // 출고 요청 수량이 현재 재고보다 많으면 외부 알림(ex. discord 등)
            if (quantity > product.getCurrentStock()) {
                notifier.notify(productName + " 제품이 " + (quantity - product.getCurrentStock()) + "개 입고가 필요합니다.");
            }

            OutboundOrderItem orderItem = mapper.toEntity(outboundOrder, product, quantity);

            orderItemList.add(orderItem);
        }
        outboundOrderItemRepository.saveAll(orderItemList);

        return mapper.toDto(outboundOrder.getId(), outboundOrderRequestDto.getProducts(), destination);
    }

    // 출고 등록
    @Transactional
    public List<OutboundResponseDto> createOutbound(OutboundRequestDto outboundRequestDto) {
        // 출고 요청 제품들 가져와서 map
        Map<String, OutboundOrderItem> outboundOrderItemMap = getOutboundOrderItemMap(outboundRequestDto.getOutboundId());

        ArrayList<Outbound> outboundList = new ArrayList<>();
        ArrayList<OutboundResponseDto> responseDtoList = new ArrayList<>();

        for (ProductDto product : outboundRequestDto.getProductList()) {
            String productName = product.getProduct();
            int quantity = product.getQuantity();
            OutboundOrderItem orderItem = outboundOrderItemMap.get(productName);

            if (orderItem == null) {
                continue;
            }
            Outbound outbound = new Outbound(quantity, orderItem);
            outboundList.add(outbound);

            // 재고 감소
            int updatedStock = updateStock(orderItem, quantity);

            // 재고가 임계치보다 낮으면 알림
            int threshold = orderItem.getProduct().getThreshold();
            if (updatedStock <= threshold) {
                notifier.notify(productName + " 제품의 재고가 " + updatedStock + "개 입니다. 재고 수량을 " + threshold + "개가 넘도록 채워주세요.");
            }

            // 출고한 수량 업데이트
            int releasedQuantity = orderItem.getReleasedQuantity();
            orderItem.setReleasedQuantity(releasedQuantity + quantity);

            // 출고 요청 상태 변경
            setOutboundOrderStatus(orderItem);

            OutboundResponseDto responseDto = new OutboundResponseDto(productName, quantity, updatedStock);
            responseDtoList.add(responseDto);
        }
        outboundRepository.saveAll(outboundList);

        return responseDtoList;
    }

    private int updateStock(OutboundOrderItem orderItem, int quantity) {
        int currentStock = orderItem.getProduct().getCurrentStock();
        log.info("current stock: {}", currentStock);

        if (currentStock >= quantity) {
            int updatedStock = currentStock - quantity;
            log.info("updated stock: {}", updatedStock);

            orderItem.getProduct().setCurrentStock(updatedStock);
            outboundOrderItemRepository.save(orderItem);

            return updatedStock;
        } else {
            throw new IllegalArgumentException("not enough stock");
        }
    }

    private static void setOutboundOrderStatus(OutboundOrderItem orderItem) {
        if (orderItem.getReleasedQuantity() < orderItem.getRequiredQuantity()) {
            orderItem.setStatus(OrderStatus.IN_PROGRESS.toString());
        }
        if (orderItem.getReleasedQuantity() >= orderItem.getRequiredQuantity()) {
            orderItem.setStatus(OrderStatus.COMPLETED.toString());
        }
    }

    private Map<String, OutboundOrderItem> getOutboundOrderItemMap(Long outboundOrderId) {
        List<OutboundOrderItem> outboundOrderItemList = outboundOrderItemRepository.findByOutboundOrderId(outboundOrderId);

        return outboundOrderItemList.stream().collect(Collectors.toMap(
                outboundOrderItem -> outboundOrderItem.getProduct().getName(),
                outboundOrderItem -> outboundOrderItem
        ));
    }


    // 출고 등록 & 처리(멀티스레딩 적용)
    public List<OutboundResponseDto> createOutboundWithMultiThreading(OutboundRequestDto outboundRequestDto) {
//        Map<String, OutboundOrderItem> orderItemMap = getOutboundOrderItemMapForMultiThreading(outboundRequestDto.getOutboundId());

        // Thread-safe 리스트 사용
        List<Outbound> completedOutboundList = Collections.synchronizedList(new ArrayList<>());
        List<OutboundResponseDto> results = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        // 병렬 처리
        List<CompletableFuture<OutboundResponseDto>> futures = outboundRequestDto.getProductList()
                .stream()
                .map(productDto -> CompletableFuture.supplyAsync(
                        () -> outboundProcessorService.processOutbound(productDto, completedOutboundList), executorService
                ).exceptionally(throwable -> {
                    // 개별 실패 기록 및 null 반환
                            String errorMsg = "제품 " + productDto.getProduct() + " 처리 실패: " + throwable.getMessage();
                            synchronized (failures) {
                                failures.add(errorMsg);
                            }
                            log.error(errorMsg, throwable);
                            return null;
                        }
                ))
                .toList();

        // 결과 수집 (실패한 것들은 제외)
        for (CompletableFuture<OutboundResponseDto> future : futures) {
            try {
                OutboundResponseDto result = future.join();
                if (result != null) {
                    results.add(result);
                }
            } catch (CompletionException e) {
                // exceptionally에서 처리되지 않은 예외들
                log.error("Unexpected completion exception", e);
            }
        }

        // 성공한 출고서들만 저장
        if (!completedOutboundList.isEmpty()) {
            outboundRepository.saveAll(completedOutboundList);
        }

        // 실패가 있으면 경고 로그 출력하지만 전체를 실패시키지는 않음
        if (!failures.isEmpty()) {
            log.warn("일부 출고 처리 실패: {}", String.join(", ", failures));
        }

        return results;
    }

    private Map<String, OutboundOrderItem> getOutboundOrderItemMapForMultiThreading(Long outboundOrderId) {
        List<OutboundOrderItem> outboundOrderItemList = outboundOrderItemRepository.findOutboundOrderItemsWithProductByOutboundOrderId(outboundOrderId);

        return outboundOrderItemList.stream().collect(Collectors.toMap(
                outboundOrderItem -> outboundOrderItem.getProduct().getName(),
                outboundOrderItem -> outboundOrderItem
        ));
    }
}
