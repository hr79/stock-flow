package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outboundorder.OutboundOrderItem;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.notification.Notifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboundStockUpdater {
    private final @Qualifier("discordNotifier") Notifier notifier;

    public int getUpdatedStock(OutboundOrderItem orderItem, int quantity) {
        Product product = orderItem.getProduct();
        int currentStock = product.getCurrentStock();
        log.info("current stock: {}", currentStock);
        int threshold = product.getThreshold();

        if (currentStock < quantity) {
            notifyThreshold(currentStock, quantity, product);
            throw new IllegalArgumentException("not enough stock");
        }
        currentStock = product.decrease(quantity);
        // 재고가 임계치보다 낮으면 알림
        if (product.isBelowThreshold()) {
            notifyThreshold(currentStock, threshold, product);
        }
        // 출고서에서 출고한 수량 업데이트
        orderItem.increaseReleasedQuantity(quantity);

        // 출고 요청 상태 변경
        orderItem.applyStatus();

        return currentStock;
    }

    private void notifyThreshold(int currentStock, int requiredStock, Product product) {
        String message = product.getName() + " 제품의 재고가 " + currentStock + "개 입니다. 재고 수량을 " + requiredStock + "개가 넘도록 채워주세요.";
        notifier.notify(message);
    }
}
