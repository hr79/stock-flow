package com.example.stockflow.domain.outbound.dto;

import com.example.stockflow.domain.product.ProductDto;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateOutboundResponseDto {
    private Long outboundOrderId;
    private String destination;
    private List<ProductDto> productDtoList;

    public CreateOutboundResponseDto(Long outboundOrderId, String destination, List<ProductDto> productDtoList) {
        this.outboundOrderId = outboundOrderId;
        this.destination = destination;
        this.productDtoList = productDtoList;
    }
}
