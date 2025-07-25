package com.example.stockflow.domain.product;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private BigDecimal price;

    @Setter
    @Column
    private int currentStock;

    @Column
    private int threshold; // 임계치 : 재고가 임계치 이하일때 알림

    @Version
    private Long version;

    @Builder
    public Product(String name, BigDecimal price, int currentStock, Integer threshold) {
        this.name = name;
        this.price = price;
        this.currentStock = currentStock;
        this.threshold = (threshold == null) ? 100 : threshold;
    }
}
