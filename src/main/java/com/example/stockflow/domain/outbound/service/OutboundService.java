package com.example.stockflow.domain.outbound.service;

import com.example.stockflow.domain.outbound.*;
import com.example.stockflow.domain.outbound.service.OutboundServiceInterface.OutboundCreationService;
import com.example.stockflow.domain.outboundorder.OutboundRequestDto;
import com.example.stockflow.domain.outboundorder.OutboundResponseDto;
import com.example.stockflow.domain.outboundorder.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Slf4j
@Service
public class OutboundService {

    private final OutboundRepository outboundRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private final OutboundProcessorService outboundProcessorService;
    private final OutboundCreationService outboundCreationService;

    // 출고 등록
    @Transactional
    public List<OutboundResponseDto> createOutbound(OutboundRequestDto outboundRequestDto) {
        return outboundCreationService.createOutbound(outboundRequestDto);
    }

    // 출고 등록 & 처리(멀티스레딩 적용)
    public List<OutboundResponseDto> createOutboundWithMultiThreading(OutboundRequestDto outboundRequestDto) {
        // Thread-safe 리스트 사용
        List<Outbound> completedOutboundList = Collections.synchronizedList(new ArrayList<>());
        List<OutboundResponseDto> results = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        // 병렬 처리
        List<CompletableFuture<OutboundResponseDto>> futures = outboundRequestDto.getProductList()
                .stream()
                .map(productDto -> CompletableFuture.supplyAsync(
                        () -> outboundProcessorService.processOutbound(productDto, completedOutboundList), executorService
                ).exceptionally(throwable -> {
                    // 개별 실패 기록 및 null 반환
                            String errorMsg = "제품 " + productDto.getProduct() + " 처리 실패: " + throwable.getMessage();
                            synchronized (failures) {
                                failures.add(errorMsg);
                            }
                            log.error(errorMsg, throwable);
                            return null;
                        }
                ))
                .toList();

        // 결과 수집 (실패한 것들은 제외)
        for (CompletableFuture<OutboundResponseDto> future : futures) {
            try {
                OutboundResponseDto result = future.join();
                if (result != null) {
                    results.add(result);
                }
            } catch (CompletionException e) {
                // exceptionally에서 처리되지 않은 예외들
                log.error("Unexpected completion exception", e);
            }
        }

        // 성공한 출고서들만 저장
        if (!completedOutboundList.isEmpty()) {
            outboundRepository.saveAll(completedOutboundList);
        }

        // 실패가 있으면 경고 로그 출력하지만 전체를 실패시키지는 않음
        if (!failures.isEmpty()) {
            log.warn("일부 출고 처리 실패: {}", String.join(", ", failures));
        }

        return results;
    }
}
