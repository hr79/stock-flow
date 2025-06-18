package com.example.stockflow.domain.inbound;

import lombok.Getter;

import java.util.List;

@Getter
public class InboundRequestDto {
    private List<com.example.stockflow.domain.product.ProductDto> products;
    private Long purchaseOrderId;
    private String supplierName;

    public static class ProductDto {
        private String product;
        private int quantity;

        public ProductDto(String product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
