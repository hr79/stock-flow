package com.example.stockflow.domain.outbound;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboundRequestRepository extends JpaRepository<OutboundRequest, Long> {
}
