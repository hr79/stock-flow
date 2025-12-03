package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.Outbound;
import com.example.stockflow.domain.outbound.OutboundRepository;
import com.example.stockflow.domain.outbound.service.OutboundItemProcessor.ItemProcessorResults;
import com.example.stockflow.domain.outboundorder.OutboundOrderItem;
import com.example.stockflow.domain.outboundorder.OutboundOrderItemRepository;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OutboundBatchProcessor {
    private final OutboundItemProcessor itemProcessor;
    private final OutboundRepository outboundRepository;
    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final ProductRepository productRepository;

    public List<OutboundResponseDto> createOutbound(List<ProductDto> productDtoList, Map<String, OutboundOrderItem> orderItemMap) {
        List<OutboundResponseDto> responseDtoList = new ArrayList<>();
        List<Outbound> outboundList = new ArrayList<>();

        for (ProductDto product : productDtoList) {
            ItemProcessorResults results = itemProcessor.process(product, orderItemMap);
            if (results == null) continue;
            Outbound outbound = results.outbound();
            OutboundResponseDto responseDto = results.outboundResponseDto();
            responseDtoList.add(responseDto);
            outboundList.add(outbound);
        }
        List<OutboundOrderItem> orderItemList = outboundList.stream().map(outbound -> outbound.getOutboundOrderItem()).toList();
        List<Product> productList = orderItemList.stream().map(orderItem -> orderItem.getProduct()).toList();

        productRepository.saveAll(productList);
        outboundRepository.saveAll(outboundList);
        outboundOrderItemRepository.saveAll(orderItemList);

        return responseDtoList;
    }
}

