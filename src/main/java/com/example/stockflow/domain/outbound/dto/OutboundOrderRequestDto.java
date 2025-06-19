package com.example.stockflow.domain.outbound.dto;

import com.example.stockflow.domain.product.ProductDto;
import lombok.Getter;

import java.util.List;

@Getter
public class OutboundOrderRequestDto {
    private List<ProductDto> products;
    private String destination;
}
