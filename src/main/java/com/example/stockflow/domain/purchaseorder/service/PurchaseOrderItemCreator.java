package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseOrderItemCreator {
    public PurchaseOrderItem create(PurchaseOrderItem item) {
        return item;
    }
}
