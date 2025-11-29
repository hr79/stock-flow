package com.example.stockflow.domain.inbound.service;

import com.example.stockflow.domain.inbound.InboundRequestDto;
import com.example.stockflow.domain.inbound.InboundResponseDto;
import com.example.stockflow.domain.inbound.service.InboundRequestValidator.ResultData;
import com.example.stockflow.domain.inbound.service.PurchaseOrderInboundServiceInterface.PurchaseOrderInboundRegistrationService;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import com.example.stockflow.domain.supplier.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderInboundRegistrationServiceImpl implements PurchaseOrderInboundRegistrationService{
    private final InboundRequestValidator inboundRequestValidator;
    private final InboundCreator inboundCreator;

    @Override
    public List<InboundResponseDto> registerInbound(InboundRequestDto inboundRequestDto) {
        log.info(":::: 입고 등록");
        List<ProductDto> inboundProducts = inboundRequestDto.getProducts();

        ResultData results = inboundRequestValidator.getSupplierAndOrderItems(inboundRequestDto);
        Supplier supplier = results.supplier();
        Map<String, PurchaseOrderItem> orderItemMap = results.purchaseOrderItems();

        return inboundCreator.createInbound(inboundProducts, orderItemMap, supplier);
    }
}
