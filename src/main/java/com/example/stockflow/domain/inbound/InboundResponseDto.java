package com.example.stockflow.domain.inbound;

import lombok.Getter;

@Getter
public class InboundResponseDto {
    private String product;
    private int quantity;
    private int updatedStock;
    private String supplierName;

    public InboundResponseDto(String product, int quantity, int updatedStock, String supplierName) {
        this.product = product;
        this.quantity = quantity;
        this.updatedStock = updatedStock;
        this.supplierName = supplierName;
    }
}
