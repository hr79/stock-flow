package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderDetailResponseDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderRequestDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderResponseDto;

import java.util.List;

public class PurchaseOrderServiceInterface {
    // 발주 생성
    public interface PurchaseOrderCreationService{
        PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderRequestDto requestDto);
    }

    // 발주 조회
    public interface PurchaseOrderQueryService {
        // 목록
        List<PurchaseOrderDetailResponseDto> getPurchaseOrderList();

        // 단건 조회
        PurchaseOrderDetailResponseDto getPurchaseOrder(String id);
    }

    // 발주 수정
    public interface PurchaseOrderUpdateService {
        PurchaseOrderDetailResponseDto updatePurchaseOrder(String id, PurchaseOrderRequestDto requestDto);
    }
}
