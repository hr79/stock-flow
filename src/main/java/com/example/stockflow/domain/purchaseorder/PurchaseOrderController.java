package com.example.stockflow.domain.purchaseorder;

import com.example.stockflow.common.ApiResponse;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderDetailResponseDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderRequestDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // 발주 등록
    @PostMapping("/purchase-order")
    public ApiResponse<?> createPurchaseOrder(@RequestBody PurchaseOrderRequestDto requestDto) {
        PurchaseOrderResponseDto respDto = purchaseOrderService.createPurchaseOrder(requestDto);

        return ApiResponse.success("/purchase-order", respDto);
    }

    // 발주 목록 조회
    @GetMapping("/purchase-order")
    public ApiResponse<?> getPurchaseOrderList() {
        List<PurchaseOrderDetailResponseDto> responseDtoList = purchaseOrderService.getPurchaseOrderList();
        return ApiResponse.success( "/purchase-order", responseDtoList);
    }




}
