package com.example.stockflow.domain.outboundorder.service;

import com.example.stockflow.domain.outbound.dto.CreateOutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.CreateOutboundResponseDto;
import com.example.stockflow.domain.outboundorder.*;
import com.example.stockflow.domain.outboundorder.service.OutboundOrderServiceInterface.OutboundOrderCreationService;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.notification.Notifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboundOrderCreationServiceImpl implements OutboundOrderCreationService {
    private final OutboundOrderRepository outboundOrderRepository;
    private final ProductRepository productRepository;
    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final OutboundOrderMapper mapper;
    private final @Qualifier("discordNotifier") Notifier notifier;

    public CreateOutboundResponseDto createOutboundOrder(CreateOutboundRequestDto createOutboundRequestDto) {
        String destination = createOutboundRequestDto.getDestination();
        OutboundOrder outboundOrder = OutboundOrder.builder().destination(destination).build();

        List<OutboundOrderItem> orderItemList = new ArrayList<>();

        List<ProductDto> requestProducts = createOutboundRequestDto.getProducts();
        for (ProductDto productDto : requestProducts) {
            String productName = productDto.getProduct();
            Product product = productRepository.findByName(productName).orElseThrow(() -> new IllegalArgumentException("not found product : " + productName));
            int quantity = productDto.getQuantity();

            // 출고 요청 수량이 현재 재고보다 많으면 외부 알림(ex. discord 등)
            if (quantity > product.getCurrentStock()) {
                notifier.notify(productName + " 제품이 " + (quantity - product.getCurrentStock()) + "개 입고가 필요합니다.");
            }

            OutboundOrderItem orderItem = mapper.toEntity(outboundOrder, product, quantity);

            orderItemList.add(orderItem);
        }
        outboundOrderRepository.save(outboundOrder);
        outboundOrderItemRepository.saveAll(orderItemList);

        return new CreateOutboundResponseDto(outboundOrder.getId(), destination, requestProducts);
    }

}
