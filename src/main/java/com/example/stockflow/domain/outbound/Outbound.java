package com.example.stockflow.domain.outbound;

import com.example.stockflow.model.BaseEntity;
import com.example.stockflow.model.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 출고
@Entity
@Table(name = "outbound")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbound extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private OutboundOrderItem outboundOrderItem;


    public Outbound(int quantity, OutboundOrderItem outboundOrderItem) {
        this.quantity = quantity;
        this.outboundOrderItem = outboundOrderItem;

    }
}
