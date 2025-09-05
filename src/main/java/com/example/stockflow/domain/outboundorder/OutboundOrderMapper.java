package com.example.stockflow.domain.outboundorder;


import com.example.stockflow.domain.outbound.dto.CreateOutboundResponseDto;
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

    public CreateOutboundResponseDto toDto(Long outboundRequestId, List<ProductDto> productDtoList, String destination) {
        return new CreateOutboundResponseDto(outboundRequestId, destination, productDtoList);
    }
}
