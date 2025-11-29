package com.example.stockflow.domain.product;

import com.example.stockflow.domain.outboundorder.OutboundOrderItem;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;

    // 부모 트랜잭션에 참여하도록 트랜잭션 어노테이션 제거
    public int checkAndUpdateStockWithRetry(OutboundOrderItem orderItem, int outboundQuantity) {
        int maxRetries = 3;
        Long productId = orderItem.getProduct().getId();
        Long orderItemId = orderItem.getId();
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("No product found with id: " + productId));

        for (int retryCount = 1; retryCount <= maxRetries; retryCount++) {
            try {
                log.info("orderItemId: {}", orderItemId);
                log.info("retryCount : {}", retryCount);
                log.info("product name: {}", product.getName());
                log.info("product id: {}", productId);
                log.info("product current stock: {}", product.getCurrentStock());

                if (retryCount != 1) { // 낙관적 락에 의한 재시도일 때는 제품 엔티티를 db에서 다시 가져온다.
                    log.info("재시도, 제품 엔티티를 다시 조회합니다.");
                    product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("id에 해당하는 제품이 존재하지 않습니다."));
                }

                int currentStock = product.getCurrentStock();

                if (outboundQuantity > currentStock) {
                    throw new IllegalArgumentException("출고량이 현재 재고보다 많습니다. 현재 재고: " + currentStock + ", 출고량: " + outboundQuantity);
                }

                int updatedStock = product.decrease(outboundQuantity);
                productRepository.saveAndFlush(product);

                return updatedStock;
            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                log.warn("낙관적 락 충돌, 재시도 {}회: {}", retryCount, e.getMessage());
                if (retryCount == maxRetries) {
                    throw new ConcurrencyFailureException("재고 업데이트 실패 (동시성 충돌)");
                }
                // 재시도 전 backoff
                try {
                    long baseWait = 500L * (1L << (retryCount - 1)); // 지수 백오프: 500ms, 1000ms, 2000ms, 4000ms...
                    long jitter = (long)(Math.random() * baseWait * 0.5); // 기본 대기 시간의 최대 50%까지 랜덤하게 추가
                    Thread.sleep(baseWait + jitter);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                // 기타 예외들은 재시도하지 않고 바로 던짐
                log.error("재고 업데이트 중 예상치 못한 오류: {}", e.getMessage());
                throw e;
            }
        }
        throw new ConcurrencyFailureException("재고 업데이트 실패: 최대 재시도 횟수 초과");
    }
}
