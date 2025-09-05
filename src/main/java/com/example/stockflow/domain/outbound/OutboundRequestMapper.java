package com.example.stockflow.domain.outbound;


import com.example.stockflow.domain.outbound.dto.CreateOutboundResponseDto;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboundRequestMapper {
    public OutboundRequestItem toEntity(OutboundRequest outboundRequest, Product product, int quantity) {
        return OutboundRequestItem.builder()
                .outboundRequest(outboundRequest)
                .product(product)
                .requiredQuantity(quantity)
                .releasedQuantity(0)
                .build();
    }

    public CreateOutboundResponseDto toDto(Long outboundRequestId, List<ProductDto> productDtoList, String destination) {
        return new CreateOutboundResponseDto(outboundRequestId, destination, productDtoList);
    }
}
