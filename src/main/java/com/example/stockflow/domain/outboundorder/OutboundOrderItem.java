package com.example.stockflow.domain.outboundorder;

import com.example.stockflow.domain.outbound.Outbound;
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

    @Column(nullable = false)
    private int releasedQuantity;

    @Column(nullable = false)
    private OrderStatus status;

    @Version
    private Long version;

    @Builder
    public OutboundOrderItem(OutboundOrder outboundOrder, Product product, int requiredQuantity, int releasedQuantity, OrderStatus status) {
        this.outboundOrder = outboundOrder;
        this.product = product;
        this.requiredQuantity = requiredQuantity;
        this.releasedQuantity = releasedQuantity;
        this.status = (status != null) ? status : OrderStatus.REQUESTED;
    }

    public int increaseReleasedQuantity(int quantity){
        this.releasedQuantity += quantity;
        return this.releasedQuantity;
    }

    public void changeStatus(OrderStatus status){
        this.status = status;
    }

    public OrderStatus applyStatus() {
        if (this.releasedQuantity == 0) {
            return this.status = OrderStatus.REQUESTED;
        } else if (this.releasedQuantity >= this.requiredQuantity) {
            return this.status = OrderStatus.COMPLETED;
        } else {
            return this.status = OrderStatus.IN_PROGRESS;
        }
    }

    public Outbound createOutbound(int quantity){
        if (!this.product.decrease(quantity)){
            return null;
        }
        increaseReleasedQuantity(quantity);
        applyStatus();
        return new Outbound(quantity, this);
    }
}
