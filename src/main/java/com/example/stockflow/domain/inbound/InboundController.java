package com.example.stockflow.domain.inbound;

import com.example.stockflow.common.ApiResponse;
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
    private final InboundService inboundService;

    // 입고 등록
    @PostMapping("/warehousing")
    public ApiResponse<?> registerWarehousing(@RequestBody InboundRequestDto requestDto) {
        List<InboundResponseDto> responseDtoList = inboundService.registerWarehousing(requestDto);
        return ApiResponse.success("/warehousing", responseDtoList);
    }
}
