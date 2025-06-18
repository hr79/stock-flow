package com.example.stockflow.domain.purchaseorder;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PurchaseOrderMapper {

    public PurchaseOrderItem toEntity(PurchaseOrder purchaseOrder, ProductDto productDto, Product product) {
        int quantity = productDto.getQuantity();

        BigDecimal totalPriceOfProduct = product.getPrice().multiply(new BigDecimal(quantity));

        return PurchaseOrderItem.builder()
                .product(product)
                .purchaseOrder(purchaseOrder)
                .requiredQuantity(quantity)
                .receivedQuantity(0)
                .totalPrice(totalPriceOfProduct)
                .build();
    }


}
