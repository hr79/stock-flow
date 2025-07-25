package com.example.stockflow.domain.outbound;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboundOrderItemRepository extends JpaRepository<OutboundOrderItem, Long> {
    List<OutboundOrderItem> findByOutboundOrderId(Long outoundOrderId);

    @Query("SELECT oi FROM OutboundOrderItem oi JOIN FETCH oi.product WHERE oi.outboundOrder.id = :outboundOrderId")
    List<OutboundOrderItem> findOutboundOrderItemsWithProductByOutboundOrderId(Long outboundOrderId);
}
