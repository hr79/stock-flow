package com.example.stockflow.domain.purchaseorder;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
    @EntityGraph(attributePaths = {"product"})
    Optional<List<PurchaseOrderItem>> findAllByPurchaseOrderId(Long purchaseOrderId);
}
