package com.example.stockflow.domain.purchaseorder;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.domain.purchaseorder.dto.ItemDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderDetailResponseDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderRequestDto;
import com.example.stockflow.domain.purchaseorder.dto.PurchaseOrderResponseDto;
import com.example.stockflow.model.OrderStatus;
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

    // 발주 단건 조회
    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponseDto getPurchaseOrder(String id) {
        Long purchaseOrderId = Long.parseLong(id);
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId).orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
        List<PurchaseOrderItem> purchaseOrderItemList = purchaseOrderItemRepository.findAllByPurchaseOrderId(purchaseOrderId).orElseThrow(() -> new IllegalArgumentException("상세 발주 물품들을 찾을 수 없습니다"));
        List<ItemDto> itemDtoList = new ArrayList<>();

        for (PurchaseOrderItem orderItem : purchaseOrderItemList) {
            ItemDto itemDto = ItemDto.builder()
                    .productName(orderItem.getProduct().getName())
                    .currentStock(orderItem.getProduct().getCurrentStock())
                    .requiredQuantity(orderItem.getRequiredQuantity())
                    .receivedQuantity(orderItem.getReceivedQuantity())
                    .status(orderItem.getStatus())
                    .build();
            itemDtoList.add(itemDto);
        }

        return new PurchaseOrderDetailResponseDto(purchaseOrderId, itemDtoList, purchaseOrder.getCreatedAt().toString());
    }

    // 발주 수정
    @Transactional
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
            PurchaseOrderItem orderItem = orderItemMap.get(product.getProduct());

            if (orderItem == null) {
                throw new IllegalArgumentException("없는 발주 상품입니다.");
            }

            // 발주 진행 상황이 request 상태이면 발주 변경
            if (orderItem.getStatus().equals(OrderStatus.REQUESTED.toString())) {
                log.info(":::: 발주가 대기 상태입니다.");
                log.info("before : {}", orderItem.getRequiredQuantity());

                orderItem.setRequiredQuantity(product.getQuantity());

                log.info("after : {}", orderItem.getRequiredQuantity());

                ItemDto itemDto = ItemDto.builder()
                        .productName(product.getProduct())
                        .requiredQuantity(product.getQuantity())
                        .build();

                itemDtoList.add(itemDto);
            }
        }
        return PurchaseOrderDetailResponseDto.builder().purchaseOrderId(purchaseOrderId).orderItems(itemDtoList).build();
    }
}
