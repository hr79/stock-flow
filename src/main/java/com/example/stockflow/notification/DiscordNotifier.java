package com.example.stockflow.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component("discordNotifier")
@Slf4j
public class DiscordNotifier implements Notifier {
    @Value("${notifier.discord}") private static String WEBHOOK_URL;

    private final WebClient webClient;

    public DiscordNotifier() {
        this.webClient = WebClient.create();
    }

    @Override
    public void notify(String message) {
        log.info(":::: DiscordNotifier");
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", message);

        webClient.post()
                .uri(WEBHOOK_URL)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        success -> log.info("입고 요청 알림 전송 성공"),
                        error -> log.warn("입고 요청 알림 전송 실패", error)
                );
    }
}
