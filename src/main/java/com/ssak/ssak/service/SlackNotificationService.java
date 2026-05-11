package com.ssak.ssak.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class SlackNotificationService {

    @Value("${custom.slack.webhook-url}")
    private String url;

    @Async
    public void sendError(Exception e, String method, String requestUrl) {
        // 1. 현재 시간
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 2. 에러가 발생한 지점 찾기 (패키지명 "com.ssak.ssak"이 포함된 첫 줄)
        String location = Arrays.stream(e.getStackTrace())
                .filter(ste -> ste.getClassName().contains("com.ssak.ssak"))
                .findFirst()
                .map(StackTraceElement::toString)
                .orElse("알 수 없는 위치");

        RestTemplate restTemplate = new RestTemplate();

        // 슬랙으로 보낼 데이터 조립
        Map<String, Object> body = new HashMap<>();
        String errorMessage = String.format(
                "🚨 *[서버 에러 발생]* \n" +
                        "*발생 시간:* %s\n" +
                        "*요청 경로:* %s %s \n" +
                        "*발생 위치:* `%s`\n" +
                        "*에러 종류:* %s \n" +
                        "*에러 메시지:* %s",
                time, method, requestUrl, location,
                e.getClass().getSimpleName(), e.getMessage()
        );

        body.put("text", errorMessage);

        // 실제 전송
        restTemplate.postForEntity(url, body, String.class);
    }
}