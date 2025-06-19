package com.example.stockflow.domain.outbound;

import com.example.stockflow.domain.outbound.dto.OutboundOrderRequestDto;
import com.example.stockflow.domain.outbound.dto.OutboundOrderResponseDto;
import com.example.stockflow.domain.outbound.dto.OutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.OutboundResponseDto;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.model.OrderStatus;
import com.example.stockflow.notification.Notifier;
import com.example.stockflow.domain.product.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OutboundService {
    private final ProductRepository productRepository;
    private final OutboundOrderRepository outboundOrderRepository;
    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final OutboundRepository outboundRepository;
    private final OutboundOrderMapper mapper;
    private final Notifier notifier;

    public OutboundService(ProductRepository productRepository, OutboundOrderRepository outboundOrderRepository, OutboundOrderItemRepository outboundOrderItemRepository, OutboundRepository outboundRepository, OutboundOrderMapper mapper, @Qualifier("discordNotifier") Notifier notifier) {
        this.productRepository = productRepository;
        this.outboundOrderRepository = outboundOrderRepository;
        this.outboundOrderItemRepository = outboundOrderItemRepository;
        this.outboundRepository = outboundRepository;
        this.mapper = mapper;
        this.notifier = notifier;
    }

    // 출고 요청
    @Transactional
    public OutboundOrderResponseDto createOutboundRequest(OutboundOrderRequestDto outboundOrderRequestDto) {
        String destination = outboundOrderRequestDto.getDestination();
        OutboundOrder outboundOrder = OutboundOrder.builder().destination(destination).build();
        outboundOrderRepository.save(outboundOrder);

        List<OutboundOrderItem> orderItemList = new ArrayList<>();

        for (ProductDto productDto : outboundOrderRequestDto.getProducts()) {
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
        outboundOrderItemRepository.saveAll(orderItemList);

        return mapper.toDto(outboundOrder.getId(), outboundOrderRequestDto.getProducts(), destination);
    }

    // 출고 등록
    @Transactional
    public List<OutboundResponseDto> registerOutbound(OutboundRequestDto outboundRequestDto) {
        Map<String, OutboundOrderItem> outboundOrderItemMap = getOutboundOrderItemMap(outboundRequestDto);

        ArrayList<Outbound> outboundList = new ArrayList<>();
        ArrayList<OutboundResponseDto> responseDtoList = new ArrayList<>();

        for (ProductDto product : outboundRequestDto.getProductList()) {
            String productName = product.getProduct();
            int quantity = product.getQuantity();
            OutboundOrderItem orderItem = outboundOrderItemMap.get(productName);

            if (orderItem == null) {
                continue;
            }
            Outbound outbound = new Outbound(quantity, orderItem);
            outboundList.add(outbound);

            // 재고 감소
            int updatedStock = updateStock(orderItem, quantity);

            // 출고한 수량 업데이트
            int releasedQuantity = orderItem.getReleasedQuantity();
            orderItem.setReleasedQuantity(releasedQuantity + quantity);

            // 출고 요청 상태 변경
            setOutboundOrderStatus(orderItem);

            OutboundResponseDto responseDto = new OutboundResponseDto(productName, quantity, updatedStock);
            responseDtoList.add(responseDto);
        }
        outboundRepository.saveAll(outboundList);

        return responseDtoList;
    }

    private int updateStock(OutboundOrderItem orderItem, int quantity) {
        int currentStock = orderItem.getProduct().getCurrentStock();
        log.info("current stock: {}", currentStock);

        if (currentStock >= quantity) {
            int updatedStock = currentStock - quantity;
            log.info("updated stock: {}", updatedStock);

            orderItem.getProduct().setCurrentStock(updatedStock);

            return updatedStock;
        } else {
            throw new IllegalArgumentException("not enough stock");
//            return currentStock;
        }
    }

    private static void setOutboundOrderStatus(OutboundOrderItem orderItem) {
        if (orderItem.getReleasedQuantity() < orderItem.getRequiredQuantity()) {
            orderItem.setStatus(OrderStatus.IN_PROGRESS.toString());
        }
        if (orderItem.getReleasedQuantity() >= orderItem.getRequiredQuantity()) {
            orderItem.setStatus(OrderStatus.COMPLETED.toString());
        }
    }

    private Map<String, OutboundOrderItem> getOutboundOrderItemMap(OutboundRequestDto outboundRequestDto) {
        List<OutboundOrderItem> outboundOrderItemList = outboundOrderItemRepository.findByOutboundOrderId(outboundRequestDto.getOutboundId());
        return outboundOrderItemList.stream().collect(Collectors.toMap(
                outboundOrderItem -> outboundOrderItem.getProduct().getName(),
                outboundOrderItem -> outboundOrderItem
        ));
    }
}
