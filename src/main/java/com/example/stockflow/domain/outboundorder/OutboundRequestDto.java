package com.example.stockflow.domain.outboundorder;

import com.example.stockflow.domain.product.ProductDto;
import lombok.Getter;

import java.util.List;

@Getter
public class OutboundRequestDto {
    private Long outboundId;
    private List<ProductDto> productList;

    public OutboundRequestDto(Long outboundId, List<ProductDto> productList) {
        this.outboundId = outboundId;
        this.productList = productList;
    }
}
