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

        log.info(":::: 현재 제품 재고: {}", product.getCurrentStock());

            // 재고 수량 증가
            int increasedStock = product.increase(quantity);
            productRepository.save(product);
            log.info(":::: 증가한 재고 수량: {}", increasedStock);

            // 입고된 수량 기록
            log.info(":::: before 받은수량: {}", orderItem.getReceivedQuantity());
            int updatedReceivedQuantity = orderItem.increaseReceivedQuantity(quantity);
            log.info(":::: after 받은수량: {}", updatedReceivedQuantity);

            if (updatedReceivedQuantity < orderItem.getRequiredQuantity()) {
                // 입고된 수량이 요청 수량보다 아직 적으면 발주 in progress 상태
                orderItem.changeStatus(OrderStatus.IN_PROGRESS.toString());
            } else {
                // 입고 수량이 요청한 수량을 채우고 그 이상이면 발주 completed 상태
                orderItem.changeStatus(OrderStatus.COMPLETED.toString());
            }
            purchaseOrderItemRepository.save(orderItem);

            return increasedStock;
    }
}
