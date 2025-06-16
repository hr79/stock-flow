package com.example.stockflow.domain.supplier;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String supplierName;

    @Column
    private String contactNumber;

    @Column
    private String email;

    @Builder
    public Supplier(String supplierName, String contactNumber, String email) {
        this.supplierName = supplierName;
        this.contactNumber = contactNumber;
        this.email = email;
    }
}
