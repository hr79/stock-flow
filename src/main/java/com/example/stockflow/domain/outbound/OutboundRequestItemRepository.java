package com.example.stockflow.domain.outbound;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboundRequestItemRepository extends JpaRepository<OutboundRequestItem, Long> {
    List<OutboundRequestItem> findByOutboundRequestId(Long outoundRequestId);

    @Query("SELECT oi FROM OutboundRequestItem oi JOIN FETCH oi.product WHERE oi.outboundRequest.id = :outboundOrderId")
    List<OutboundRequestItem> findOutboundRequestItemsWithProductByOutboundRequestId(Long outboundRequestId);
}
