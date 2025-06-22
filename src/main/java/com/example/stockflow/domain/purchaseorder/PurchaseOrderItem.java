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

    @Setter
    @Column(nullable = false)
    private int requiredQuantity;

    @Setter
    @Column(nullable = false)
    private int receivedQuantity;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Setter
    @Column(nullable = false)
    private String status;

    @Builder
    public PurchaseOrderItem(PurchaseOrder purchaseOrder, Product product, int requiredQuantity, int receivedQuantity, BigDecimal totalPrice, String status) {
        this.purchaseOrder = purchaseOrder;
        this.product = product;
        this.requiredQuantity = requiredQuantity;
        this.receivedQuantity = receivedQuantity;
        this.totalPrice = totalPrice;
        this.status = (status == null) ? OrderStatus.REQUESTED.toString() : status;
    }
}
