package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.*;
import com.example.stockflow.domain.outbound.service.OutboundServiceInterface.OutboundCreationService;
import com.example.stockflow.domain.outboundorder.OutboundRequestDto;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;
import com.example.stockflow.domain.outboundorder.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class OutboundService {
    private final OutboundCreationService outboundCreationService;

    // 출고 등록
    @Transactional
    public List<OutboundResponseDto> createOutbound(OutboundRequestDto outboundRequestDto) {
        return outboundCreationService.createOutbound(outboundRequestDto);
    }
}
