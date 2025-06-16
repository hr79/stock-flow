package com.example.stockflow.domain.purchaseorder;

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
    private BigDecimal totalPrice;

    @Column
    private String status;

    @Builder
    public PurchaseOrder(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public PurchaseOrder(){
        this.status = OrderStatus.REQUESTED.toString();
    }
}
