package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.*;
import com.example.stockflow.domain.outbound.dto.OutboundResponseDto;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.model.OrderStatus;
import com.example.stockflow.notification.Notifier;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class OutboundProcessorService {

    private final ProductRepository productRepository;
    private final OutboundOrderRepository outboundOrderRepository;
    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final OutboundRepository outboundRepository;
    private final OutboundOrderMapper mapper;
    private final Notifier notifier;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public OutboundProcessorService(ProductRepository productRepository, OutboundOrderRepository outboundOrderRepository, OutboundOrderItemRepository outboundOrderItemRepository, OutboundRepository outboundRepository, OutboundOrderMapper mapper, @Qualifier("discordNotifier") Notifier notifier) {
        this.productRepository = productRepository;
        this.outboundOrderRepository = outboundOrderRepository;
        this.outboundOrderItemRepository = outboundOrderItemRepository;
        this.outboundRepository = outboundRepository;
        this.mapper = mapper;
        this.notifier = notifier;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboundResponseDto proccessOutbound(ProductDto productDto, Map<String, OutboundOrderItem> orderItemMap) {
        String productName = productDto.getProduct();
        OutboundOrderItem orderItem = orderItemMap.get(productName);
        if (orderItem == null) {
            throw new IllegalArgumentException("출고 요청 제품이 아닙니다. : " + productName);
        }

        // 재고감소(낙관적 락)
        int quantity = productDto.getQuantity();
        int updatedStock = updateStockWithRetry(orderItem, quantity);

        // 업데이트된 재고가 임계치보다 낮으면 알람
        Product product = orderItem.getProduct();
        int threshold = product.getThreshold();

        if (updatedStock <= threshold) {
            notifier.notify(product.getName() + " 제품의 재고가 " + updatedStock + "개 입니다. 재고 수량을 " + threshold + "개가 넘도록 채워주세요.");
        }

        // 출고한 수량 업데이트
        int releasedQuantity = orderItem.getReleasedQuantity();
        orderItem.setReleasedQuantity(releasedQuantity + quantity);

        // 출고 상태 변경
        setOutboundOrderStatus(orderItem);

        return new OutboundResponseDto(productName, quantity, updatedStock);
    }

    private int updateStockWithRetry(OutboundOrderItem orderItem, int quantity) {
        int maxRetries = 3;

        for (int retryCount = 1; retryCount <= maxRetries; retryCount++) {
            try {
                int currentStock = orderItem.getProduct().getCurrentStock();
                if (currentStock < quantity) {
                    throw new IllegalArgumentException("재고가 부족합니다");
                }
                int updatedQuantity = currentStock - quantity;
                orderItem.getProduct().setCurrentStock(updatedQuantity);
                outboundOrderItemRepository.saveAndFlush(orderItem);

                return updatedQuantity;
            } catch (OptimisticLockException e) {
                log.warn("낙관적 락 충돌, 재시도 {}회", retryCount);
                if (retryCount == maxRetries) {
                    throw new ConcurrencyFailureException("재고 업데이트 실패 (동시성 충돌)");
                }
                // 재시도 전 잠깐 대기 (backoff 전략)
                try {
                    Thread.sleep(50L * retryCount);
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new ConcurrencyFailureException("재고 업데이트 실패");
    }

    private static void setOutboundOrderStatus(OutboundOrderItem orderItem) {
        if (orderItem.getReleasedQuantity() < orderItem.getRequiredQuantity()) {
            orderItem.setStatus(OrderStatus.IN_PROGRESS.toString());
        }
        if (orderItem.getReleasedQuantity() >= orderItem.getRequiredQuantity()) {
            orderItem.setStatus(OrderStatus.COMPLETED.toString());
        }
    }

}
