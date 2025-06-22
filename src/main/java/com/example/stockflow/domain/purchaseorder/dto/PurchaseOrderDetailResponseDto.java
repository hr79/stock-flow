package com.example.stockflow.domain.purchaseorder.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PurchaseOrderDetailResponseDto {
    private Long purchaseOrderId;
    private List<ItemDto> orderItems;
    private String createdAt;

    @Builder
    public PurchaseOrderDetailResponseDto(Long purchaseOrderId, List<ItemDto> orderItems, String createdAt) {
        this.purchaseOrderId = purchaseOrderId;
        this.orderItems = orderItems;
        this.createdAt = createdAt;
    }
}
