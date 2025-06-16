package com.example.stockflow.domain.outbound;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.model.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 출고요청서 품목 상세
public class OutboundOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private OutboundOrder outboundOrder;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(nullable = false)
    private int requiredQuantity;

    @Setter
    @Column(nullable = false)
    private int releasedQuantity;

    @Setter
    @Column(nullable = false)
    private String status;

    @Builder
    public OutboundOrderItem(OutboundOrder outboundOrder, Product product, int requiredQuantity, int releasedQuantity, String status) {
        this.outboundOrder = outboundOrder;
        this.product = product;
        this.requiredQuantity = requiredQuantity;
        this.releasedQuantity = releasedQuantity;
        this.status = (status != null) ? status : OrderStatus.REQUESTED.toString();
    }
}
