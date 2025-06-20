package com.example.stockflow.domain.purchaseorder;

import com.example.stockflow.common.ApiResponse;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderDetailResponseDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderRequestDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.mapstruct.ap.internal.model.SupportingMappingMethod;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // 발주 등록
    @Operation(summary = "발주 등록(주문 요청)")
    @PostMapping("/purchase-order")
    public ApiResponse<?> createPurchaseOrder(@RequestBody PurchaseOrderRequestDto requestDto) {
        PurchaseOrderResponseDto respDto = purchaseOrderService.createPurchaseOrder(requestDto);

        return ApiResponse.success("/purchase-order", respDto);
    }

    @Operation(summary = "발주 조회")
    @GetMapping("/purchase-order/{id}")
    public ApiResponse<?> getPurchaseOrder(@PathVariable String id) {
        PurchaseOrderDetailResponseDto responseDto = purchaseOrderService.getPurchaseOrder(id);
        return ApiResponse.success("/purchase-order", responseDto);
    }

    // 발주 목록 조회
    @Operation(summary = "발주 목록 조회")
    @GetMapping("/purchase-order")
    public ApiResponse<?> getPurchaseOrderList() {
        List<PurchaseOrderDetailResponseDto> responseDtoList = purchaseOrderService.getPurchaseOrderList();
        return ApiResponse.success( "/purchase-order", responseDtoList);
    }
}
