package com.example.stockflow.domain.outbound;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OutboundOrderItemRepository extends JpaRepository<OutboundOrderItem, Long> {
    List<OutboundOrderItem> findByOutboundOrderId(Long outboundOrderId);

    @Query("SELECT oi FROM OutboundOrderItem oi JOIN FETCH oi.product WHERE oi.outboundOrder.id = :outboundOrderId")
    List<OutboundOrderItem> findOutboundOrderItemsWithProductByOutboundOrderId(Long outboundOrderId);

    Optional<OutboundOrderItem> findByProduct_Name(String name);
}
