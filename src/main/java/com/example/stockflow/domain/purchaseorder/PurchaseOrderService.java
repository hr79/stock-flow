package com.example.stockflow.domain.purchaseorder;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.domain.purchaseorder.dto.ItemDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderDetailResponseDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderRequestDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderItemRepository orderItemRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;

    // 발주 요청서
    @Transactional
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
//        List<String> nameList = new ArrayList<>();
//        for (ProductDto productDto : productsAndQuantity) {
//            nameList.add(productDto.getProduct());
//        }
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

//        for (ProductDto productAndQuantity : productsAndQuantity) {
//            Product product = productRepository.findByName(productAndQuantity.getProduct())
//                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productAndQuantity.getProduct()));
//            PurchaseOrderItem purchaseOrderItem = purchaseOrderMapper.toEntity(purchaseOrder, productAndQuantity, product);
//
//            purchaseOrderItems.add(purchaseOrderItem);
//
//            // 제품 * 수량 가격 합산
//            allProductsTotalPrice = allProductsTotalPrice.add(purchaseOrderItem.getTotalPrice());
//        }
        purchaseOrder.setTotalPrice(allProductsTotalPrice);

        return purchaseOrder;
    }

    // 발주 목록 조회
    @Transactional(readOnly = true)
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
}
