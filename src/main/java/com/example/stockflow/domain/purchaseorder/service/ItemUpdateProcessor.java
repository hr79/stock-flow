package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import com.example.stockflow.domain.purchaseorder.dto.ItemDto;
import com.example.stockflow.model.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ItemUpdateProcessor {
    public ItemDto process(Map<String, PurchaseOrderItem> orderItemMap, ProductDto productDto) {
        String productName = productDto.getProduct();
        PurchaseOrderItem orderItem = orderItemMap.get(productName);

        if (orderItem == null) {
            throw new IllegalArgumentException("없는 발주 상품입니다.");
        }

        // 발주 진행 상황이 request 상태이면 발주 변경
        if (orderItem.getStatus().equals(OrderStatus.REQUESTED)) {
            log.info(":::: 발주가 대기 상태입니다.");
            log.info("before 요청 수량: {}", orderItem.getRequiredQuantity());

            int requiredQuantity = orderItem.setRequiredQuantity(productDto.getQuantity());

            log.info("after 요청 수량: {}", requiredQuantity);

            return ItemDto.builder()
                    .productName(productName)
                    .requiredQuantity(requiredQuantity)
                    .build();
        }
        return null;
    }
}
