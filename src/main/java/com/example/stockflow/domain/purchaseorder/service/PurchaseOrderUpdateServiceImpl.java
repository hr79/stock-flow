package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItemRepository;
import com.example.stockflow.domain.purchaseorder.dto.ItemDto;
import com.example.stockflow.domain.purchaseorder.service.PurchaseOrderServiceInterface.PurchaseOrderUpdateService;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderDetailResponseDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderUpdateServiceImpl implements PurchaseOrderUpdateService {
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ItemUpdateProcessor itemUpdateProcessor;

    @Override
    public PurchaseOrderDetailResponseDto updatePurchaseOrder(String id, PurchaseOrderRequestDto requestDto) {
        log.info(":::: updatePurchaseOrder");
        log.info("id: {}", id);

        List<ProductDto> products = requestDto.getProducts();
        List<ItemDto> itemDtoList = new ArrayList<>();
        long purchaseOrderId = Long.parseLong(id);

        List<PurchaseOrderItem> purchaseOrderItemList = purchaseOrderItemRepository.findAllByPurchaseOrderId(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주 상품을 찾을 수 없습니다."));

        Map<String, PurchaseOrderItem> orderItemMap = purchaseOrderItemList.stream()
                .collect(Collectors.toMap(
                        purchaseOrderItem -> purchaseOrderItem.getProduct().getName(),
                        purchaseOrderItem -> purchaseOrderItem
                ));

        for (ProductDto product : products) {
            ItemDto itemDto = itemUpdateProcessor.process(orderItemMap, product);
            itemDtoList.add(itemDto);
        }

        return PurchaseOrderDetailResponseDto.builder()
                .purchaseOrderId(purchaseOrderId)
                .orderItems(itemDtoList)
                .build();
    }
}

