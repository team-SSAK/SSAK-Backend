package com.ssak.ssak.config;

import jakarta.servlet.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class MDCFilter implements Filter {
    private static final String TRACE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 1. 요청이 들어올 때 고유한 UUID 생성 후에 MDC에 'traceId'라는 이름으로 저장
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            // 2. 요청이 끝날 때 메모리 누수를 방지하기 위해 MDC 비우기
            MDC.remove(TRACE_ID);
        }
    }
}
