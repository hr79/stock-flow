package com.example.stockflow.domain.purchaseorder.service;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.product.ProductDto;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.domain.purchaseorder.PurchaseOrder;
import com.example.stockflow.domain.purchaseorder.PurchaseOrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductProcessor {
    private final ProductRepository productRepository;

    public ProductProcessorResults process(List<String> productNames, List<ProductDto> productsAndQuantity) {
        List<Product> productList = productRepository.findProductsByNameIn(productNames);
        log.info("productList: {}", productList);

        Map<String, Product> productsInDb = productList.stream().collect(Collectors.toMap(
                product -> product.getName(),
                product -> product
        ));

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        List<PurchaseOrderItem> orderItemList = new ArrayList<>();
        Map<String, PurchaseOrderItem> orderItemMap = new ConcurrentHashMap<>();

        for (ProductDto productDto : productsAndQuantity) {
            String productName = productDto.getProduct();
            Product product = productsInDb.get(productName);
            if (product == null) {
                log.error("상품 정보를 찾을 수 없습니다: {} 상품을 등록해주세요", productName);
                continue;
            }
            int quantity = productDto.getQuantity();

            if (orderItemMap.get(productName) != null){
                log.info("이미 동일한 상품이 발주 목록에 존재합니다. 수량과 총 가격을 업데이트합니다: {}", productName);
                PurchaseOrderItem orderItem = orderItemMap.get(productName);
                orderItem.addQuantity(quantity);
                continue;
            }
            PurchaseOrderItem orderItem = purchaseOrder.createOrderItem(product, quantity);

            orderItemList.add(orderItem);
            orderItemMap.put(productName, orderItem);
        }
        return new ProductProcessorResults(purchaseOrder, orderItemList);
    }
}
