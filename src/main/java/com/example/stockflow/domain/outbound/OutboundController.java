package com.example.stockflow.domain.outbound;

import com.example.stockflow.common.ApiResponse;
import com.example.stockflow.domain.outbound.dto.OutboundOrderRequestDto;
import com.example.stockflow.domain.outbound.dto.OutboundOrderResponseDto;
import com.example.stockflow.domain.outbound.dto.OutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.OutboundResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class OutboundController {
    private final OutboundService outboundService;

    // 출고 요청
    @PostMapping("/outbound-request")
    public ApiResponse<?> createOutboundRequest(@RequestBody OutboundOrderRequestDto requestDto) {
        OutboundOrderResponseDto responseDto = outboundService.createOutboundRequest(requestDto);

        return ApiResponse.success("/outbound-request", responseDto);
    }

    // 출고 등록
    @PostMapping("/outbound")
    public ApiResponse<?> registerOutbound(@RequestBody OutboundRequestDto requestDto) {
        List<OutboundResponseDto> responseDtoList = outboundService.registerOutbound(requestDto);

        return ApiResponse.success("/outbound", responseDtoList);
    }
}
