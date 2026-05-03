package com.ssak.ssak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("========================================");
        System.out.println("🔥 CORS Bean 생성됨!");
        System.out.println("========================================");

        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 출처
        List<String> allowedOrigins = Arrays.asList(
                "http://localhost:8081",          // 프론트 테스트
                "http://localhost:8080",          // 로컬 테스트 서버
                "https://seederslab.n-e.kr",       // 배포 서버
                "https://ssak--fwdv0q1ejo.expo.app", // 프론트 배포 서버
                "https://appleid.apple.com"         // Apple 로그인의 form_post 요청
        );
        configuration.setAllowedOrigins(allowedOrigins);

        //프론트엔드에서 헤더에 있는 토큰을 읽어야 한다면
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 포함 여부
        configuration.setAllowCredentials(true);

        // ⭐ preflight 캐싱 시간 설정
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}