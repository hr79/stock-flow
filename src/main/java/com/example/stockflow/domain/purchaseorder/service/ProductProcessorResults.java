package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.purchaseorder.PurchaseOrder;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;

import java.util.List;

public record ProductProcessorResults(PurchaseOrder purchaseOrder, List<PurchaseOrderItem> orderItemList){}
