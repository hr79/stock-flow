package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.service.OutboundServiceInterface.OutboundCreationService;
import com.example.stockflow.domain.outboundorder.OutboundOrderItem;
import com.example.stockflow.domain.outboundorder.OutboundOrderItemRepository;
import com.example.stockflow.domain.outboundorder.OutboundRequestDto;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboundCreationServiceImpl implements OutboundCreationService{
    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final OutboundCreator outboundCreator;

    @Override
    public List<OutboundResponseDto> createOutbound(OutboundRequestDto outboundRequestDto) {
        // 출고 요청 제품들 가져와서 map
        List<OutboundOrderItem> outboundOrderItemList = outboundOrderItemRepository.findByOutboundOrderId(outboundRequestDto.getOutboundId());

        Map<String, OutboundOrderItem> orderItemMap = outboundOrderItemList.stream()
                .collect(Collectors.toConcurrentMap(
                outboundOrderItem -> outboundOrderItem.getProduct().getName(),
                outboundOrderItem -> outboundOrderItem
        ));

        return outboundCreator.createOutbound(outboundRequestDto.getProductList(), orderItemMap);
    }
}
