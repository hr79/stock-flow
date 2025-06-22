package com.example.stockflow.domain.product;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductDto {
    private String product;
    private int quantity;

    public ProductDto(String product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}
