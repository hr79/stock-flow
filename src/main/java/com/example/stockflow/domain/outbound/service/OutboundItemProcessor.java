package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.Outbound;
import com.example.stockflow.domain.outboundorder.OutboundOrderItem;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboundItemProcessor {
    private final OutboundNotificationHandler outboundNotificationHandler;

    @Transactional
    public ItemProcessorResults process(ProductDto productDto, Map<String, OutboundOrderItem> orderItemMap) {
        String productName = productDto.getProduct();
        OutboundOrderItem orderItem = orderItemMap.get(productName);
        if (orderItem == null) {
            return null;
        }
        int quantity = productDto.getQuantity();
        // 출고서에서 출고한 수량 및 상태 변경
        Outbound outbound = orderItem.createOutbound(quantity);
        Product product = orderItem.getProduct();
        if (outbound == null) {
            outboundNotificationHandler.failHandle(product, quantity);
            return null;
        }
        outboundNotificationHandler.successHandle(product);

        OutboundResponseDto dto = new OutboundResponseDto(productName, quantity, product.getCurrentStock());
        return new ItemProcessorResults(dto, outbound);
    }

    public record ItemProcessorResults(OutboundResponseDto outboundResponseDto, Outbound outbound) {
    }
}
