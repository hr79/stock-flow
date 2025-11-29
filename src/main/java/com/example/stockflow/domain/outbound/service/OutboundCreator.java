package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.Outbound;
import com.example.stockflow.domain.outbound.OutboundRepository;
import com.example.stockflow.domain.outboundorder.OutboundOrderItem;
import com.example.stockflow.domain.outboundorder.OutboundOrderItemRepository;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;
import com.example.stockflow.domain.product.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OutboundCreator {
    private final OutboundRepository outboundRepository;
    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final OutboundStockUpdater outboundStockUpdater;

    public List<OutboundResponseDto> createOutbound(List<ProductDto> productDtoList, Map<String, OutboundOrderItem> orderItemMap) {
        List<Outbound> outboundList = new ArrayList<>();
        List<OutboundResponseDto> responseDtoList = new ArrayList<>();

        for (ProductDto product : productDtoList) {
            String productName = product.getProduct();
            int quantity = product.getQuantity();
            OutboundOrderItem orderItem = orderItemMap.get(productName);

            if (orderItem == null) {
                continue;
            }
            // 해당 제품 재고 업데이트
            int updatedStock = outboundStockUpdater.getUpdatedStock(orderItem, quantity);

            outboundList.add(new Outbound(quantity, orderItem));
            responseDtoList.add(new OutboundResponseDto(productName, quantity, updatedStock));
        }
        outboundRepository.saveAll(outboundList);
        outboundOrderItemRepository.saveAll(orderItemMap.values());

        return responseDtoList;
    }
}

