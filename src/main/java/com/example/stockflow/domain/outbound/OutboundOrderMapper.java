package com.example.stockflow.domain.outbound;


import com.example.stockflow.domain.outbound.dto.OutboundOrderResponseDto;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.Product;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public OutboundOrderResponseDto toDto(Long outboundOrderId, List<ProductDto> productDtoList, String destination) {
        return new OutboundOrderResponseDto(outboundOrderId, destination, productDtoList);
    }
}
