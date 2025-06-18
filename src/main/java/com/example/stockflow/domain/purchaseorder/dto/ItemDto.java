package com.example.stockflow.domain.purchaseorder.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemDto {
    private String productName;
    private int currentStock;
    private int requiredQuantity;
    private int receivedQuantity;
    private String status;

    @Builder
    public ItemDto(String productName, int currentStock, int requiredQuantity, int receivedQuantity, String status) {
        this.productName = productName;
        this.currentStock = currentStock;
        this.requiredQuantity = requiredQuantity;
        this.receivedQuantity = receivedQuantity;
        this.status = status;
    }
}
