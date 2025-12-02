package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.purchaseorder.*;
import com.example.stockflow.domain.purchaseorder.service.PurchaseOrderServiceInterface.PurchaseOrderCreationService;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderRequestDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderCreationServiceImpl implements PurchaseOrderCreationService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository orderItemRepository;

    private final ProductProcessor productProcessor;

    @Override
    public PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderRequestDto requestDto) {
        List<ProductDto> productsAndQuantity = requestDto.getProducts();

        List<String> nameList = productsAndQuantity.stream().map(ProductDto::getProduct).toList();
        log.info("nameList: {}", nameList);
        ProductProcessorResults results = productProcessor.process(nameList, productsAndQuantity);
        PurchaseOrder purchaseOrder = results.purchaseOrder();
        List<PurchaseOrderItem> purchaseOrderItems = results.orderItemList();

        purchaseOrderRepository.save(purchaseOrder);
        orderItemRepository.saveAll(purchaseOrderItems);

        return new PurchaseOrderResponseDto(purchaseOrder.getId(), purchaseOrder.getTotalPrice());
    }

}
