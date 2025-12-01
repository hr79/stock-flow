package com.example.stockflow.domain.outboundorder.service;

import com.example.stockflow.domain.outbound.dto.CreateOutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.CreateOutboundResponseDto;
import com.example.stockflow.domain.outboundorder.service.OutboundOrderServiceInterface.OutboundOrderCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboundOrderService {
    private final OutboundOrderCreationService outboundOrderCreationService;

    @Transactional
    public CreateOutboundResponseDto createOutboundOrder(CreateOutboundRequestDto createOutboundRequestDto) {
        return outboundOrderCreationService.createOutboundOrder(createOutboundRequestDto);
    }
}
