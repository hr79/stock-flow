package com.example.stockflow.domain.outbound;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboundOrderItemRepository extends JpaRepository<OutboundOrderItem, Long> {
    List<OutboundOrderItem> findByOutboundOrderId(Long outoundOrderId);
}
