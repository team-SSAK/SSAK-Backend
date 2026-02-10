package com.ssak.ssak.config;

import com.ssak.ssak.security.JWT.JwtAuthenticationEntryPoint;
import com.ssak.ssak.security.JWT.JwtAuthenticationFilter;
import com.ssak.ssak.security.OAuth.CustomOAuth2UserService;
import com.ssak.ssak.security.OAuth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomOAuth2UserService  oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 예외 처리 (인증 실패 시)
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/authorization/**",
                                "/login/oauth2/**",
                                "/api/email/**"
                        ).permitAll()
                        //나머지는 모두 인증 필요
                        .anyRequest().authenticated()
                )

                //OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(oAuth2UserService)   //사용자 정보 처리
                        )
                        .successHandler(oAuth2SuccessHandler)   //로그인 성공 처리
                )

                //JWT 필터 추가
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
