package com.example.stockflow.domain.inbound;

import com.example.stockflow.common.ApiResponse;
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
public class InboundController {
    private final PurchaseOrderInboundService purchaseOrderInboundService;

    // 입고 등록
    @Operation(summary = "입고 등록")
    @PostMapping("/warehousing")
    public ApiResponse<?> registerWarehousing(@RequestBody InboundRequestDto requestDto) {
        List<InboundResponseDto> responseDtoList = purchaseOrderInboundService.registerInbound(requestDto);
        return ApiResponse.success("/warehousing", responseDtoList);
    }
}
