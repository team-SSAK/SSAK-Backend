package com.ssak.ssak.security.OAuth;

import com.ssak.ssak.security.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AppleOAuthController {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Value("${apple.client.id}")
    private String appleClientId;

    @Value("${apple.redirect-uri}")
    private String redirectUri;

    @GetMapping("/oauth2/authorization/login/apple")
    public void redirectToApple(HttpServletResponse response) throws IOException {
        String url = "https://appleid.apple.com/auth/authorize" +
                "?client_id=" + appleClientId +
                "&redirect_uri=" + redirectUri + // 본인 서버의 Redirect URI
                "&response_type=code" +
                "&scope=name%20email" +
                "&response_mode=form_post";
        response.sendRedirect(url);
    }

    @PostMapping(value = "/login/oauth2/code/apple/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void appleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "user", required = false) String userJson,  // 첫 로그인 시 넘어올 수 있는 유저 정보
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        // 1. 애플 서버와 통신하여 유저 고유 ID(sub) 및 이메일 획득
        CustomUserDetails userDetails = (CustomUserDetails) customOAuth2UserService.processAppleLogin(code, userJson);

        // 2. Spring Security Authentication 객체 수동 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 기존의 SuccessHandler 활용
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);
    }
}
