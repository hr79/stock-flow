package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.notification.Notifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboundNotificationHandler {
    private final @Qualifier("discordNotifier") Notifier notifier;

    // 재고부족으로 출고 실패시 알림
    public void failHandle(Product product, int quantity) {
        notify(product.getCurrentStock(), quantity, product.getName());
    }

    public void successHandle(Product product){
        // 재고가 임계치보다 낮으면 알림
        if (product.isBelowThreshold()) {
            notify(product.getCurrentStock(), product.getThreshold(), product.getName());
        }
    }

    private void notify(int currentStock, int requiredStock, String productName) {
        String message = productName + " 제품의 재고가 " + currentStock + "개 입니다. 재고 수량을 " + requiredStock + "개가 넘도록 채워주세요.";
        notifier.notify(message);
    }
}
