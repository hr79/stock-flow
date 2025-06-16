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


    @Builder
    public Product(String name, BigDecimal price, int currentStock) {
        this.name = name;
        this.price = price;
        this.currentStock = currentStock;
    }
}
