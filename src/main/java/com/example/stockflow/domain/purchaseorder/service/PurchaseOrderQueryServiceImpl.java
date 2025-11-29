package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.purchaseorder.PurchaseOrder;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItemRepository;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderRepository;
import com.example.stockflow.domain.purchaseorder.dto.ItemDto;
import com.example.stockflow.domain.purchaseorder.service.PurchaseOrderServiceInterface.PurchaseOrderQueryService;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderDetailResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderQueryServiceImpl implements PurchaseOrderQueryService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Override
    public List<PurchaseOrderDetailResponseDto> getPurchaseOrderList() {
        List<PurchaseOrderDetailResponseDto> responseDtos = new ArrayList<>();

        for (PurchaseOrder purchaseOrder : purchaseOrderRepository.findAll()) {
            List<PurchaseOrderItem> purchaseOrderItems = purchaseOrderItemRepository.findAllByPurchaseOrderId(purchaseOrder.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Purchase Order Not Found"));

            List<ItemDto> orderItems = new ArrayList<>();

            for (PurchaseOrderItem orderItem : purchaseOrderItems) {
                ItemDto itemDto = ItemDto.builder()
                        .productName(orderItem.getProduct().getName())
                        .currentStock(orderItem.getProduct().getCurrentStock())
                        .requiredQuantity(orderItem.getRequiredQuantity())
                        .receivedQuantity(orderItem.getReceivedQuantity())
                        .status(orderItem.getStatus())
                        .build();

                orderItems.add(itemDto);
            }
            responseDtos.add(new PurchaseOrderDetailResponseDto(purchaseOrder.getId(), orderItems, purchaseOrder.getCreatedAt().toString()));
        }

        return responseDtos;
    }

    @Override
    public PurchaseOrderDetailResponseDto getPurchaseOrder(String id) {
        Long purchaseOrderId = Long.parseLong(id);
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId).orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
        List<PurchaseOrderItem> purchaseOrderItemList = purchaseOrderItemRepository.findAllByPurchaseOrderId(purchaseOrderId).orElseThrow(() -> new IllegalArgumentException("상세 발주 물품들을 찾을 수 없습니다"));
        List<ItemDto> itemDtoList = new ArrayList<>();

        for (PurchaseOrderItem orderItem : purchaseOrderItemList) {
            ItemDto itemDto = ItemDto.builder()
                    .productName(orderItem.getProduct().getName())
                    .currentStock(orderItem.getProduct().getCurrentStock())
                    .requiredQuantity(orderItem.getRequiredQuantity())
                    .receivedQuantity(orderItem.getReceivedQuantity())
                    .status(orderItem.getStatus())
                    .build();
            itemDtoList.add(itemDto);
        }

        return new PurchaseOrderDetailResponseDto(purchaseOrderId, itemDtoList, purchaseOrder.getCreatedAt().toString());
    }
}
