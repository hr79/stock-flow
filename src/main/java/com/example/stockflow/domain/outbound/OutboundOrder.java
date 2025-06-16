package com.example.stockflow.domain.outbound;

import com.example.stockflow.model.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 출고요청서
public class OutboundOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String status;

    @Builder
    public OutboundOrder( String destination, String status) {
        this.destination = destination;
        this.status = (status != null) ? status : OrderStatus.REQUESTED.toString();
    }
}
