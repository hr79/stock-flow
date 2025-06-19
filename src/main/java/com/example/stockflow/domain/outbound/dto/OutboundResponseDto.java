package com.example.stockflow.domain.outbound.dto;

import lombok.Getter;

@Getter
public class OutboundResponseDto {
    private String productName;
    private int quantity;
    private int updatedStock;

    public OutboundResponseDto(String productName, int quantity, int updatedStock) {
        this.productName = productName;
        this.quantity = quantity;
        this.updatedStock = updatedStock;
    }
}
