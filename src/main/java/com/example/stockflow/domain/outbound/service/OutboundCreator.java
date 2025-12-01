package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outboundorder.OutboundOrderItem;
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
    private final OutboundItemProcessor itemProcessor;

    public List<OutboundResponseDto> createOutbound(List<ProductDto> productDtoList, Map<String, OutboundOrderItem> orderItemMap) {
        List<OutboundResponseDto> responseDtoList = new ArrayList<>();

        for (ProductDto product : productDtoList) {
            OutboundResponseDto responseDto = itemProcessor.process(product, orderItemMap);
            if (responseDto == null) continue;
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }
}

