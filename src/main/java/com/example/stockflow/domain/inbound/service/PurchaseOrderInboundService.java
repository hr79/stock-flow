package com.example.stockflow.domain.inbound.service;

import com.example.stockflow.domain.inbound.InboundRequestDto;
import com.example.stockflow.domain.inbound.InboundResponseDto;
import com.example.stockflow.domain.inbound.service.PurchaseOrderInboundServiceInterface.PurchaseOrderInboundRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderInboundService {
    private final PurchaseOrderInboundRegistrationService inboundRegistrationService;

    // 입고 등록 (by 공급업체)
    @Transactional
    public List<InboundResponseDto> registerInbound(InboundRequestDto inboundRequestDto) {
      return inboundRegistrationService.registerInbound(inboundRequestDto);
    }
}
