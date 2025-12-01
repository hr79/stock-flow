package com.example.stockflow.domain.outboundorder.service;

import com.example.stockflow.domain.outbound.dto.CreateOutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.CreateOutboundResponseDto;
import com.example.stockflow.domain.outboundorder.service.OutboundOrderServiceInterface.OutboundOrderCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboundOrderService {
    OutboundOrderCreationService outboundOrderCreationService;

    public CreateOutboundResponseDto createOutboundOrder(CreateOutboundRequestDto createOutboundRequestDto) {
        return outboundOrderCreationService.createOutboundOrder(createOutboundRequestDto);
    }
}
