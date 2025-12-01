package com.example.stockflow.domain.outboundorder;

import com.example.stockflow.common.ApiResponse;
import com.example.stockflow.domain.outbound.dto.CreateOutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.CreateOutboundResponseDto;
import com.example.stockflow.domain.outboundorder.service.OutboundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outbound-orders")
@RequiredArgsConstructor
public class OutboundOrderController {
    private final OutboundOrderService outboundOrderService;

    // 출고 요청
    @Operation(summary = "출고 요청")
    @PostMapping
    public ApiResponse<?> createOutboundRequest(@RequestBody CreateOutboundRequestDto requestDto) {
        CreateOutboundResponseDto responseDto = outboundOrderService.createOutboundOrder(requestDto);

        return ApiResponse.success("/outbound-orders", responseDto);
    }
}
