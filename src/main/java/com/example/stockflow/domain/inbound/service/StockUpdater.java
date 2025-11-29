package com.example.stockflow.domain.inbound.service;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItemRepository;
import com.example.stockflow.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockUpdater {
    private final ProductRepository productRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    public int updateCurrentStock(PurchaseOrderItem orderItem, int quantity) {
        // 재고 수량 업데이트 로직 구현
        Product product = orderItem.getProduct();

        int currentStock = product.getCurrentStock();
        log.info(":::: 현재 제품 재고: {}", currentStock);

        // 재고 수량 증가
        currentStock = product.increase(quantity);
        productRepository.save(product);
        log.info(":::: 증가한 재고 수량: {}", currentStock);

        // 입고된 수량 기록
        int currentReceivedQuantity = orderItem.getReceivedQuantity();
        log.info(":::: before 받은수량: {}", currentReceivedQuantity);
        currentReceivedQuantity = orderItem.increaseReceivedQuantity(quantity);
        log.info(":::: after 받은수량: {}", currentReceivedQuantity);

        orderItem.applyStatus();
        purchaseOrderItemRepository.save(orderItem);

        return currentStock;
    }
}
