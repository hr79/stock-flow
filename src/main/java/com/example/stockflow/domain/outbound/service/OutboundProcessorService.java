package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.*;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;
import com.example.stockflow.domain.outboundorder.OutboundOrderItem;
import com.example.stockflow.domain.outboundorder.OutboundOrderItemRepository;
import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.domain.product.StockService;
import com.example.stockflow.model.OrderStatus;
import com.example.stockflow.notification.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OutboundProcessorService {

    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final StockService stockService;
    private final Notifier notifier;
    private final ProductRepository productRepository;

    public OutboundProcessorService(
            OutboundOrderItemRepository outboundOrderItemRepository,
            StockService stockService,
            @Qualifier("discordNotifier") Notifier notifier, ProductRepository productRepository) {
        this.outboundOrderItemRepository = outboundOrderItemRepository;
        this.stockService = stockService;
        this.notifier = notifier;
        this.productRepository = productRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboundResponseDto processOutbound(ProductDto productDto, List<Outbound> processedOutboundList) {
        String productName = productDto.getProduct();

        // 반드시 fresh하게 DB에서 조회
        OutboundOrderItem orderItem = outboundOrderItemRepository.findByProduct_Name(productName)
                .orElseThrow(() -> new IllegalArgumentException("출고 요청 아이템을 찾을 수 없습니다. : " + productName));

        int outboundQuantity = productDto.getQuantity();
        if (outboundQuantity > orderItem.getRequiredQuantity()) {
            throw new IllegalArgumentException("출고량이 요청수량보다 많습니다. 요청수량: " + orderItem.getRequiredQuantity() + ", 출고량: " + outboundQuantity);
        }

        // 재고 확인 및 출고량만큼 감소(낙관적 락 적용)
        int updatedStock = stockService.checkAndUpdateStockWithRetry(orderItem, outboundQuantity);

        // fresh한 product를 db에서 가져옴
        Product product = productRepository.findByName(productName).orElseThrow(() -> new IllegalArgumentException("not found product : " + productName));

        log.info("업데이트된 재고: {}", product.getCurrentStock());

        // releasedQuantity 증가 (동기화)
        int releasedQuantity = orderItem.increaseReleasedQuantity(outboundQuantity);

        Long orderItemId = orderItem.getId();
        log.info("[증가 후] orderItemId: {}, releasedQuantity: {}", orderItemId, releasedQuantity);

        // 출고 요청 상태 변경 (fresh 인스턴스에서)
        orderItem.applyStatus();
//        setOutboundOrderStatus(orderItem);
        outboundOrderItemRepository.saveAndFlush(orderItem);

        // 출고 엔티티 생성 및 리스트에 추가
        Outbound outbound = new Outbound(outboundQuantity, orderItem);
        processedOutboundList.add(outbound);

        // 업데이트된 재고가 임계치보다 낮으면 알람
        int threshold = product.getThreshold();
        if (updatedStock <= threshold) {
            notifier.notify(productName + " 제품의 재고가 " + updatedStock + "개 입니다. 재고 수량을 " + threshold + "개가 넘도록 채워주세요.");
        }

        return new OutboundResponseDto(productName, outboundQuantity, updatedStock);
    }

//    private static void setOutboundOrderStatus(OutboundOrderItem orderItem) {
//        if (orderItem.getReleasedQuantity() < orderItem.getRequiredQuantity()) {
//            orderItem.setStatus(OrderStatus.IN_PROGRESS.toString());
//        }
//        if (orderItem.getReleasedQuantity() >= orderItem.getRequiredQuantity()) {
//            orderItem.setStatus(OrderStatus.COMPLETED.toString());
//        }
//    }
}
