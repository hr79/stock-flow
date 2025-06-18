package com.example.stockflow.domain.purchaseorder.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PurchaseOrderResponseDto {
    private Long purchaseOrderId;
    private BigDecimal totalPrice;

    public PurchaseOrderResponseDto(Long purchaseOrderId, BigDecimal totalPrice) {
        this.purchaseOrderId = purchaseOrderId;
        this.totalPrice = totalPrice;
    }

}
