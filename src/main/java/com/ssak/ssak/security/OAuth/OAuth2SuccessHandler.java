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
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. OAuth2User에서 사용자 정보 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        System.out.println("OAuth2 로그인 성공");

        // 2. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserEmail());

        // 3. Redis에 리프레시 토큰 저장
        redisTemplate.opsForValue().set(
                "RT:" + user.getUserEmail(),
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        // 4. Refresh Token을 HttpOnly 쿠키에 담기
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)      // https환경은 true
                .path("/")
                .maxAge(refreshExpiration/1000) // 초단위 설정
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // 5. 프론트엔드 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8081")
                .queryParam("token", accessToken)
                .build()
                .encode()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
