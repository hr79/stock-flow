package com.example.stockflow.domain.outboundorder;

import com.example.stockflow.domain.product.Product;
import org.springframework.stereotype.Component;

@Component
public class OutboundOrderMapper {
    public OutboundOrderItem toEntity(OutboundOrder outboundOrder, Product product, int quantity) {
        return OutboundOrderItem.builder()
                .outboundOrder(outboundOrder)
                .product(product)
                .requiredQuantity(quantity)
                .releasedQuantity(0)
                .build();
    }
}
