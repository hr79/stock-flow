package com.example.stockflow.domain.purchaseorder.dto;

import com.example.stockflow.domain.product.ProductDto;
import lombok.Getter;

import java.util.List;

@Getter
public class PurchaseOrderRequestDto {
    private List<ProductDto> products;
}
