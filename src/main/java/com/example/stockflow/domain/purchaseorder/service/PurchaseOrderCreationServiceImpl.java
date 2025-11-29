package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.domain.purchaseorder.*;
import com.example.stockflow.domain.purchaseorder.service.PurchaseOrderServiceInterface.PurchaseOrderCreationService;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderRequestDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderCreationServiceImpl implements PurchaseOrderCreationService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @Override
    public PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderRequestDto requestDto) {
        List<ProductDto> productsAndQuantity = requestDto.getProducts();
        List<PurchaseOrderItem> purchaseOrderItems = new ArrayList<>();

        PurchaseOrder purchaseOrder = createPurchaseOrder(productsAndQuantity, purchaseOrderItems);

        purchaseOrderRepository.save(purchaseOrder);
        orderItemRepository.saveAll(purchaseOrderItems);

        return new PurchaseOrderResponseDto(purchaseOrder.getId(), purchaseOrder.getTotalPrice());
    }

    private PurchaseOrder createPurchaseOrder(List<ProductDto> productsAndQuantity, List<PurchaseOrderItem> purchaseOrderItems) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        BigDecimal allProductsTotalPrice = BigDecimal.ZERO;
        List<String> nameList = productsAndQuantity.stream().map(ProductDto::getProduct).toList();
        log.info("nameList: {}", nameList);
        List<Product> productList = productRepository.findProductsByNameIn(nameList);
        log.info("productList: {}", productList);

        Map<String, ProductDto> productDtoMap = productsAndQuantity.stream()
                .collect(Collectors.toMap(
                        productDto -> productDto.getProduct(),
                        productDto -> productDto
                ));

        for (Product product : productList) {
            ProductDto productDto = productDtoMap.get(product.getName());
            PurchaseOrderItem purchaseOrderItem = purchaseOrderMapper.toEntity(purchaseOrder, productDto, product);

            purchaseOrderItems.add(purchaseOrderItem);
            allProductsTotalPrice = allProductsTotalPrice.add(purchaseOrderItem.getTotalPrice());
        }
        purchaseOrder.setTotalPrice(allProductsTotalPrice);

        return purchaseOrder;
    }
}
