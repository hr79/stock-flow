package com.example.stockflow.domain.purchaseorder.dto;

import com.example.stockflow.domain.product.ProductDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PurchaseOrderRequestDto {
    private List<ProductDto> products;
}
