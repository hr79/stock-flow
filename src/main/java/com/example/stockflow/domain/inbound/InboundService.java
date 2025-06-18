package com.example.stockflow.domain.inbound;

import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.model.OrderStatus;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import com.example.stockflow.domain.supplier.Supplier;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItemRepository;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.domain.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboundService {
    private final InboundRepository inboundRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ProductRepository productRepository;

    // 입고 등록 (by 공급업체)
    @Transactional
    public List<InboundResponseDto> registerWarehousing(InboundRequestDto inboundRequestDto) {
        log.info(":::: 입고 등록");
        List<ProductDto> inboundProducts = inboundRequestDto.getProducts();
        Map<String, PurchaseOrderItem> orderItemMap = orderItemListToMap(inboundRequestDto.getPurchaseOrderId());
        Supplier supplier = supplierRepository.findBySupplierName(inboundRequestDto.getSupplierName())
                .orElseThrow(() -> new IllegalArgumentException("not found supplier"));

        return createInbound(inboundProducts, orderItemMap, supplier);
    }

    private Map<String, PurchaseOrderItem> orderItemListToMap(Long purchaseOrderId) {
        log.info(":::: 발주 아이템 목록 가져오기");
        List<PurchaseOrderItem> purchaseOrderItems = purchaseOrderItemRepository.findAllByPurchaseOrderId(purchaseOrderId).orElseThrow(() -> new IllegalArgumentException("Purchase Order Not Found"));

        // 발주 목록 map
        Map<String, PurchaseOrderItem> toMap = new ConcurrentHashMap<>();

        for (PurchaseOrderItem purchaseOrderItem : purchaseOrderItems) {
            // key: product name, value: purchaseOrderItem
            toMap.put(purchaseOrderItem.getProduct().getName(), purchaseOrderItem);
        }
        log.info(":::: toMap:{}", toMap);

        return toMap;
    }

    private List<InboundResponseDto> createInbound(List<ProductDto> inboundProducts, Map<String, PurchaseOrderItem> orderItemMap, Supplier supplier) {
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

                Product product = orderItem.getProduct();
                // 발주에 대한 수량 업데이트
                int updatedStock = updateCurrentStock(product, quantity, orderItem);
                inboundOrders.add(new InboundResponseDto(productName, quantity, updatedStock, supplier.getSupplierName()));
            } else {
                throw new IllegalArgumentException("발주 요청한 상품이 아님");
            }
        }
        inboundRepository.saveAll(inbounds);

        return inboundOrders;
    }

    private int updateCurrentStock(Product product, Integer quantity, PurchaseOrderItem orderItem) {
        int currentStock = product.getCurrentStock();
        log.info(":::: 현재 제품 재고: {}", currentStock);

        // 재고 수량 증가
        product.setCurrentStock(currentStock + quantity);
        productRepository.save(product);
        log.info(":::: 증가한 재고 수량: {}", product.getCurrentStock());

        log.info(":::: before 요청수량: {}", orderItem.getReceivedQuantity());
        orderItem.setReceivedQuantity(orderItem.getReceivedQuantity() + quantity);
        int updatedReceivedQuantity = orderItem.getReceivedQuantity();
        log.info(":::: after 요청수량: {}", updatedReceivedQuantity);

        if (updatedReceivedQuantity < orderItem.getRequiredQuantity()) {
            // 입고 수량이 요청한 수량보다 적으면 발주 in progress 상태
            orderItem.setStatus(OrderStatus.IN_PROGRESS.toString());
        } else {
            // 입고 수량이 요청한 수량 이상이면 발주 completed 상태
            orderItem.setStatus(OrderStatus.COMPLETED.toString());
        }
        purchaseOrderItemRepository.save(orderItem);
        return currentStock + quantity;
    }

}
