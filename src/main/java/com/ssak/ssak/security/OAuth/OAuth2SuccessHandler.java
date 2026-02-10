package com.ssak.ssak.security.OAuth;

import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.security.JWT.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. OAuth2User에서 사용자 정보 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        System.out.println("OAuth2 로그인 성공");

        // 2. JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getUserEmail());

        // 3. 프론트엔드 리다이렉트 TODO: 프론트엔드 uri에 맞게 수정필요
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8081")
                .queryParam("token", token)
                .build()
                .toUriString();

        // 4. 리다이렉트 실행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
