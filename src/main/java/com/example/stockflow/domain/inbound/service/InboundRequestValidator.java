package com.example.stockflow.domain.inbound.service;

import com.example.stockflow.domain.inbound.InboundRequestDto;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItemRepository;
import com.example.stockflow.domain.supplier.Supplier;
import com.example.stockflow.domain.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class InboundRequestValidator {
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    public ResultData getSupplierAndOrderItems(InboundRequestDto inboundRequestDto){
        Supplier supplier = supplierRepository.findBySupplierName(inboundRequestDto.getSupplierName())
                .orElseThrow(() -> new IllegalArgumentException("not found supplier"));

        log.info(":::: 발주 아이템 목록 가져오기");
        List<PurchaseOrderItem> purchaseOrderItems = purchaseOrderItemRepository.findAllByPurchaseOrderId(inboundRequestDto.getPurchaseOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase Order Not Found"));

        Map<String, PurchaseOrderItem> orderItemMap = listToMap(purchaseOrderItems);

        return new ResultData(supplier, orderItemMap);
    }

    private Map<String, PurchaseOrderItem> listToMap(List<PurchaseOrderItem> purchaseOrderItems) {
        Map<String, PurchaseOrderItem> orderItemMap = new ConcurrentHashMap<>();
        for (PurchaseOrderItem purchaseOrderItem : purchaseOrderItems) {
            // key: product name, value: purchaseOrderItem
            orderItemMap.put(purchaseOrderItem.getProduct().getName(), purchaseOrderItem);
        }
        log.info(":::: orderItemMap: {}", orderItemMap);
        return orderItemMap;
    }

    public record ResultData(Supplier supplier, Map<String, PurchaseOrderItem> purchaseOrderItems) { }
}
