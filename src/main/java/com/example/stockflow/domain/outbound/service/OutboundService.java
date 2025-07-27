package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.*;
import com.example.stockflow.domain.outbound.dto.CreateOutboundRequestDto;
import com.example.stockflow.domain.outbound.dto.CreateOutboundResponseDto;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OutboundService {
    private final ProductRepository productRepository;
    private final OutboundRequestRepository outboundRequestRepository;
    private final OutboundRequestItemRepository outboundRequestItemRepository;
    private final OutboundRepository outboundRepository;
    private final OutboundRequestMapper mapper;
    private final Notifier notifier;
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private final OutboundProcessorService outboundProcessorService;

    public OutboundService(ProductRepository productRepository,
                           OutboundRequestRepository outboundRequestRepository, 
                           OutboundRequestItemRepository outboundRequestItemRepository,
                           OutboundRepository outboundRepository,
                           OutboundRequestMapper mapper,
                           @Qualifier("discordNotifier") Notifier notifier,
                           OutboundProcessorService outboundProcessorService) {
        this.productRepository = productRepository;
        this.outboundRequestRepository = outboundRequestRepository;
        this.outboundRequestItemRepository = outboundRequestItemRepository;
        this.outboundRepository = outboundRepository;
        this.mapper = mapper;
        this.notifier = notifier;
        this.outboundProcessorService = outboundProcessorService;
    }

    // 출고 요청
    @Transactional
    public CreateOutboundResponseDto createOutboundRequest(CreateOutboundRequestDto createOutboundRequestDto) {
        String destination = createOutboundRequestDto.getDestination();
        OutboundRequest outboundRequest = OutboundRequest.builder().destination(destination).build();
        outboundRequestRepository.save(outboundRequest);

        List<OutboundRequestItem> requestItemList = new ArrayList<>();

        for (ProductDto productDto : createOutboundRequestDto.getProducts()) {
            String productName = productDto.getProduct();
            Product product = productRepository.findByName(productName)
                    .orElseThrow(() -> new IllegalArgumentException("not found product : " + productName));
            int quantity = productDto.getQuantity();

            // 출고 요청 수량이 현재 재고보다 많으면 외부 알림(ex. discord 등)
            if (quantity > product.getCurrentStock()) {
                notifier.notify(productName + " 제품이 " + (quantity - product.getCurrentStock()) + "개 입고가 필요합니다.");
            }

            OutboundRequestItem requestItem = mapper.toEntity(outboundRequest, product, quantity);

            requestItemList.add(requestItem);
        }
        outboundRequestItemRepository.saveAll(requestItemList);

        return mapper.toDto(outboundRequest.getId(), createOutboundRequestDto.getProducts(), destination);
    }

    // 출고 등록
    @Transactional
    public List<OutboundResponseDto> createOutbound(OutboundRequestDto outboundRequestDto) {
        // 출고 요청 제품들 가져와서 map
        Map<String, OutboundRequestItem> outboundrequestItemMap = getOutboundrequestItemMap(outboundRequestDto.getOutboundId());

        ArrayList<Outbound> outboundList = new ArrayList<>();
        ArrayList<OutboundResponseDto> responseDtoList = new ArrayList<>();

        for (ProductDto product : outboundRequestDto.getProductList()) {
            String productName = product.getProduct();
            int quantity = product.getQuantity();
            OutboundRequestItem requestItem = outboundrequestItemMap.get(productName);

            if (requestItem == null) {
                continue;
            }
            Outbound outbound = new Outbound(quantity, requestItem);
            outboundList.add(outbound);

            // 재고 감소
            int updatedStock = updateStock(requestItem, quantity);

            // 재고가 임계치보다 낮으면 알림
            int threshold = requestItem.getProduct().getThreshold();
            if (updatedStock <= threshold) {
                notifier.notify(productName + " 제품의 재고가 " + updatedStock + "개 입니다. 재고 수량을 " + threshold + "개가 넘도록 채워주세요.");
            }

            // 출고한 수량 업데이트
            int releasedQuantity = requestItem.getReleasedQuantity();
            requestItem.setReleasedQuantity(releasedQuantity + quantity);

            // 출고 요청 상태 변경
            setOutboundOrderStatus(requestItem);

            OutboundResponseDto responseDto = new OutboundResponseDto(productName, quantity, updatedStock);
            responseDtoList.add(responseDto);
        }
        outboundRepository.saveAll(outboundList);

        return responseDtoList;
    }

    private int updateStock(OutboundRequestItem requestItem, int quantity) {
        int currentStock = requestItem.getProduct().getCurrentStock();
        log.info("current stock: {}", currentStock);

        if (currentStock >= quantity) {
            int updatedStock = currentStock - quantity;
            log.info("updated stock: {}", updatedStock);

            requestItem.getProduct().setCurrentStock(updatedStock);
            outboundRequestItemRepository.save(requestItem);

            return updatedStock;
        } else {
            throw new IllegalArgumentException("not enough stock");
        }
    }

    private static void setOutboundOrderStatus(OutboundRequestItem requestItem) {
        if (requestItem.getReleasedQuantity() < requestItem.getRequiredQuantity()) {
            requestItem.setStatus(OrderStatus.IN_PROGRESS.toString());
        }
        if (requestItem.getReleasedQuantity() >= requestItem.getRequiredQuantity()) {
            requestItem.setStatus(OrderStatus.COMPLETED.toString());
        }
    }

    private Map<String, OutboundRequestItem> getOutboundrequestItemMap(Long outboundOrderId) {
        List<OutboundRequestItem> outboundRequestItemList = outboundRequestItemRepository.findByOutboundRequestId(outboundOrderId);

        return outboundRequestItemList.stream().collect(Collectors.toMap(
                outboundrequestItem -> outboundrequestItem.getProduct().getName(),
                outboundrequestItem -> outboundrequestItem
        ));
    }


    // 출고 등록 & 처리(멀티스레딩 적용)
    public List<OutboundResponseDto> fulfillOutboundRequestWithMultiThreading(OutboundRequestDto outboundRequestDto) {
        Map<String, OutboundRequestItem> requestItemMap = getOutboundrequestItemMapForMultiThreading(outboundRequestDto.getOutboundId());

        // 병렬 처리
        List<CompletableFuture<OutboundResponseDto>> futures = outboundRequestDto.getProductList()
                .stream()
                .map(productDto -> CompletableFuture.supplyAsync(
                        () -> outboundProcessorService.proccessOutbound(productDto, requestItemMap), executorService
                ))
                .toList();

        return futures.stream()
                .map(future -> {
                            try {
                                return future.join(); // 결과 수집
                            } catch (CompletionException e) {
                                Throwable cause = e.getCause() != null ? e.getCause() : e;
                                throw new RuntimeException("출고 처리 실패 : " + cause.getMessage(), cause);
                            }
                        }
                ).toList();
    }

    private Map<String, OutboundRequestItem> getOutboundrequestItemMapForMultiThreading(Long outboundOrderId) {
        List<OutboundRequestItem> outboundRequestItemList = outboundRequestItemRepository.findOutboundRequestItemsWithProductByOutboundRequestId(outboundOrderId);

        return outboundRequestItemList.stream().collect(Collectors.toMap(
                outboundrequestItem -> outboundrequestItem.getProduct().getName(),
                outboundrequestItem -> outboundrequestItem
        ));
    }
}
