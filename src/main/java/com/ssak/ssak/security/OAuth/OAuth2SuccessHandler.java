package com.ssak.ssak.security.OAuth;

import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.security.JWT.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. OAuth2User에서 사용자 정보 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        System.out.println("OAuth2 로그인 성공");

        // 2. 임시 인증 코드 생성 (UUID) 및 redis에 저장
        String tempCode = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "OAUTH_CODE:" + tempCode,
                user.getUserEmail(),
                1,
                TimeUnit.MINUTES
        );

        // 3. 프론트엔드로 임시 코드 전달
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("code", tempCode)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
