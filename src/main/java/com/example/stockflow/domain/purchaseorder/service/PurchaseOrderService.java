package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderDetailResponseDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderRequestDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderResponseDto;
import com.example.stockflow.domain.purchaseorder.service.PurchaseOrderServiceInterface.PurchaseOrderCreationService;
import com.example.stockflow.domain.purchaseorder.service.PurchaseOrderServiceInterface.PurchaseOrderQueryService;
import com.example.stockflow.domain.purchaseorder.service.PurchaseOrderServiceInterface.PurchaseOrderUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderCreationService purchaseOrderCreationService;
    private final PurchaseOrderQueryService purchaseOrderQueryService;
    private final PurchaseOrderUpdateService purchaseOrderUpdateService;

    // 발주 요청서
    @Transactional
    public PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderRequestDto requestDto) {
        return purchaseOrderCreationService.createPurchaseOrder(requestDto);
    }

    // 발주 목록 조회
    @Transactional(readOnly = true)
    public List<PurchaseOrderDetailResponseDto> getPurchaseOrderList() {
        return purchaseOrderQueryService.getPurchaseOrderList();
    }

    // 발주 단건 조회
    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponseDto getPurchaseOrder(String id) {
        return purchaseOrderQueryService.getPurchaseOrder(id);
    }

    // 발주 수정
    @Transactional
    public PurchaseOrderDetailResponseDto updatePurchaseOrder(String id, PurchaseOrderRequestDto requestDto) {
        return purchaseOrderUpdateService.updatePurchaseOrder(id, requestDto);
    }
}
