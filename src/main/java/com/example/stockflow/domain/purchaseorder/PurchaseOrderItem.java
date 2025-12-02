package com.example.stockflow.domain.purchaseorder;

import com.example.stockflow.model.BaseEntity;
import com.example.stockflow.model.OrderStatus;
import com.example.stockflow.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Product product;

    @Column(nullable = false)
    private int requiredQuantity;

    @Column(nullable = false)
    private int receivedQuantity = 0;

    @Column(nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private OrderStatus status;

    @Builder
    public PurchaseOrderItem(PurchaseOrder purchaseOrder, Product product, int requiredQuantity, int receivedQuantity, BigDecimal totalPrice, OrderStatus status) {
        this.purchaseOrder = purchaseOrder;
        this.product = product;
        this.requiredQuantity = requiredQuantity;
        this.receivedQuantity = receivedQuantity;
        this.totalPrice = totalPrice;
        this.status = (status == null) ? OrderStatus.REQUESTED : status;
    }

    public int increaseReceivedQuantity(int quantity) {
        this.receivedQuantity += quantity;
        return this.receivedQuantity;
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }

    public int setRequiredQuantity(int quantity) {
        this.requiredQuantity = quantity;
        return this.requiredQuantity;
    }

    public void increaseRequiredQuantity(int quantity) {
        this.requiredQuantity += quantity;
    }

    public OrderStatus applyStatus() {
        if (this.receivedQuantity == 0) {
            return this.status = OrderStatus.REQUESTED;
        } else if (this.receivedQuantity >= this.requiredQuantity) {
            return this.status = OrderStatus.COMPLETED;
        } else {
            return this.status = OrderStatus.IN_PROGRESS;
        }
    }

    public BigDecimal calculateTotalPrice() {
        return this.totalPrice = this.product.getPrice().multiply(BigDecimal.valueOf((this.requiredQuantity)));
    }

    public void addQuantity(int quantity){
        increaseRequiredQuantity(quantity);
        calculateTotalPrice();
    }
}
