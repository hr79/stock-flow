package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.Outbound;
import com.example.stockflow.domain.outbound.OutboundRepository;
import com.example.stockflow.domain.outboundorder.OutboundOrderItem;
import com.example.stockflow.domain.outboundorder.OutboundOrderItemRepository;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;
import com.example.stockflow.domain.product.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboundItemProcessor {
    private final OutboundStockUpdater outboundStockUpdater;
    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final OutboundRepository outboundRepository;

    @Transactional
    public OutboundResponseDto process(ProductDto product, Map<String, OutboundOrderItem> orderItemMap) {
        String productName = product.getProduct();
        OutboundOrderItem orderItem = orderItemMap.get(productName);
        if (orderItem == null) {
            return null;
        }
        int quantity = product.getQuantity();
        // 해당 제품 재고 업데이트
        int updatedStock = outboundStockUpdater.getUpdatedStock(orderItem, quantity);

        outboundRepository.save(new Outbound(quantity, orderItem));
        outboundOrderItemRepository.save(orderItem);

        return new OutboundResponseDto(productName, quantity, updatedStock);
    }
}
