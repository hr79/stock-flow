package com.example.stockflow.domain.inbound;

import com.example.stockflow.model.BaseEntity;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import com.example.stockflow.domain.supplier.Supplier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

// 입고
@Entity
@Table(name = "inbound")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inbound extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @JoinColumn(name = "purchase_order_item_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseOrderItem purchaseOrderItem;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Supplier supplier;

    @Builder
    public Inbound(int quantity, PurchaseOrderItem purchaseOrderItem, Supplier supplier) {
        this.quantity = quantity;
        this.purchaseOrderItem = purchaseOrderItem;
        this.supplier = supplier;
    }
}
