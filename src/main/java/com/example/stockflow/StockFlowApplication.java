package com.example.stockflow;

import com.example.stockflow.domain.product.Product;
import com.example.stockflow.domain.supplier.Supplier;
import com.example.stockflow.domain.product.ProductRepository;
import com.example.stockflow.domain.supplier.SupplierRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class StockFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockFlowApplication.class, args);
    }

    @Bean
    CommandLineRunner saveDummyProducts(ProductRepository productRepository, SupplierRepository supplierRepository) {
        return args -> {
            Product product1 = Product.builder()
                    .name("product_name_1")
                    .price(new BigDecimal(1000))
                    .currentStock(0)
                    .build();

            Product product2 = Product.builder()
                    .name("product_name_2")
                    .price(new BigDecimal(2000))
                    .currentStock(0)
                    .build();

            Product product3 = Product.builder()
                    .name("product_name_3")
                    .price(new BigDecimal(3000))
                    .currentStock(0)
                    .build();

            Product product4 = Product.builder()
                    .name("product_name_4")
                    .price(new BigDecimal(4000))
                    .currentStock(0)
                    .build();

            Product product5 = Product.builder()
                    .name("product_name_5")
                    .price(new BigDecimal(5000))
                    .currentStock(0)
                    .build();

            List<Product> products = Arrays.asList(product1, product2, product3, product4, product5);
            productRepository.saveAll(products);

            Supplier supplier1 = new Supplier("supplier_1", "01012345678", "supplier@email.com");

            supplierRepository.save(supplier1);
        };
    }
}
