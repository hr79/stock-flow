package com.example.stockflow.domain.outboundorder.service;

import com.example.stockflow.domain.outbound.dto.CreateOutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.CreateOutboundResponseDto;

public class OutboundOrderServiceInterface {
    public interface OutboundOrderCreationService {
        CreateOutboundResponseDto createOutboundOrder(CreateOutboundRequestDto createOutboundRequestDto);
    }
}
