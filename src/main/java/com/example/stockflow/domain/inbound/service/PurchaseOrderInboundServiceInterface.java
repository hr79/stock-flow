package com.example.stockflow.domain.inbound.service;

import com.example.stockflow.domain.inbound.InboundRequestDto;
import com.example.stockflow.domain.inbound.InboundResponseDto;

import java.util.List;

public class PurchaseOrderInboundServiceInterface {
    // 입고 등록(by 공급업체)
    public interface PurchaseOrderInboundRegistrationService {
        List<InboundResponseDto> registerInbound(InboundRequestDto inboundRequestDto);
    }
}
