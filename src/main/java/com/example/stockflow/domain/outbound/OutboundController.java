package com.example.stockflow.domain.outbound;

import com.example.stockflow.common.ApiResponse;
import com.example.stockflow.domain.outbound.dto.CreateOutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.CreateOutboundResponseDto;
import com.example.stockflow.domain.outboundorder.OutboundRequestDto;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;
import com.example.stockflow.domain.outbound.service.OutboundService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "출고 요청")
    @PostMapping("/outbound-request")
    public ApiResponse<?> createOutboundRequest(@RequestBody CreateOutboundRequestDto requestDto) {
        CreateOutboundResponseDto responseDto = outboundService.createOutboundOrder(requestDto);

        return ApiResponse.success("/outbound-request", responseDto);
    }

    // 출고 등록
    @Operation(summary = "출고 등록")
    @PostMapping("/outbound")
    public ApiResponse<?> registerOutbound(@RequestBody OutboundRequestDto requestDto) {
        List<OutboundResponseDto> responseDtoList = outboundService.createOutboundWithMultiThreading(requestDto);

        return ApiResponse.success("/outbound", responseDtoList);
    }
}
