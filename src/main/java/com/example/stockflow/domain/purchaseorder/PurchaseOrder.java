package com.example.stockflow.domain.purchaseorder;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.model.BaseEntity;
import com.example.stockflow.model.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order")
@Getter
public class PurchaseOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column
    private String status;

    @Builder
    public PurchaseOrder(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public PurchaseOrder(){
        this.status = OrderStatus.REQUESTED.toString();
    }

    public PurchaseOrderItem createOrderItem(Product product, int quantity){
        PurchaseOrderItem orderItem = PurchaseOrderItem.builder()
                .purchaseOrder(this)
                .product(product)
                .requiredQuantity(quantity)
                .build();
        orderItem.calculateTotalPrice();
        calculateTotalPrice(orderItem.getTotalPrice());

        return orderItem;
    }
    public void calculateTotalPrice(BigDecimal price){
        this.totalPrice = this.totalPrice.add(price);
    }
}
