package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outboundorder.OutboundRequestDto;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;

import java.util.List;

public class OutboundServiceInterface {
    public interface OutboundCreationService {
        List<OutboundResponseDto> createOutbound(OutboundRequestDto outboundRequestDto);
    }
}
