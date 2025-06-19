package com.example.stockflow.domain.outbound.dto;

import com.example.stockflow.domain.product.ProductDto;
import lombok.Getter;

import java.util.List;

@Getter
public class OutboundRequestDto {
    private Long outboundId;
    private List<ProductDto> productList;
}
