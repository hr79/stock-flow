package com.example.stockflow.domain.inbound.service;

import com.example.stockflow.domain.inbound.Inbound;
import com.example.stockflow.domain.inbound.InboundRepository;
import com.example.stockflow.domain.inbound.InboundRequestDto;
import com.example.stockflow.domain.inbound.InboundResponseDto;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import com.example.stockflow.domain.supplier.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboundCreator {

    private final InboundRepository inboundRepository;
    private final StockUpdater stockUpdater;

    public List<InboundResponseDto> createInbound(List<ProductDto> inboundProducts, Map<String, PurchaseOrderItem> orderItemMap, Supplier supplier) {
        List<InboundResponseDto> inboundOrders = new ArrayList<>();
        List<Inbound> inbounds = new ArrayList<>();

        for (ProductDto inboundProduct : inboundProducts) {
            String productName = inboundProduct.getProduct();
            int quantity = inboundProduct.getQuantity();

            PurchaseOrderItem orderItem = orderItemMap.get(productName);

            if (orderItem != null) {
                log.info(":::: 발주 신청한 상품 맞음");
                // 입고 기록 저장
                Inbound inbound = new Inbound(quantity, orderItem, supplier);
                inbounds.add(inbound);

                // 발주에 대한 수량 업데이트
                // updatedStock: 업데이트된 재고 수량
                int updatedStock = stockUpdater.updateCurrentStock(orderItem, quantity);

                inboundOrders.add(new InboundResponseDto(productName, quantity, updatedStock, supplier.getSupplierName()));
            } else {
                throw new IllegalArgumentException("발주 요청한 상품이 아님");
            }
        }
        inboundRepository.saveAll(inbounds);

        return inboundOrders;
    }
}
