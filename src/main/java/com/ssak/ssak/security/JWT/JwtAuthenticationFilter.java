package com.ssak.ssak.security.JWT;

import com.ssak.ssak.exception.ErrorCode;
import com.ssak.ssak.security.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // OPTIONS 요청은 JWT 검증을 하지 않고 즉시 다음 필터(CORS 필터 등)로 넘김
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // permitAll 경로는 JWT 검증 스킵
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/") ||
                requestURI.startsWith("/oauth2/") ||
                requestURI.startsWith("/login/oauth2/") ||
                requestURI.startsWith("/api/email/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 요청 헤더에서 JWT 토큰 추출
            String token = jwtTokenProvider.resolveToken(request);

            // 2. 토큰이 있고 유효한지 검증
            if (token != null && jwtTokenProvider.validateToken(token)) {

                // Redis 블랙리스트 체크 - 해당 데이터가 Redis에 존재 시 이미 로그아웃 한 사용자 토큰
                String isLogout = (String) redisTemplate.opsForValue().get("BL:" + token);
                if(isLogout != null) {
                    logger.warn("블랙리스트에 등록된 토큰으로 접근 시도");
                    sendErrorResponse(response, ErrorCode.LOGOUT_TOKEN);
                }

                // 토큰 타입이 ACCESS 아니면 차단 (리프레시 토큰으로 API 호출 방지)
                if (!"ACCESS".equals(jwtTokenProvider.getTokenType(token))) {
                    sendErrorResponse(response, ErrorCode.ACCESS_TOKEN_REQUIRED);
                    return;
                }

                // 3. 토큰에서 사용자 이메일 추출
                String email = jwtTokenProvider.getEmail(token);

                // 4. 이메일로 사용자 정보 조회
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // 5. 인증 객체 생성
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, ErrorCode.ACCESS_TOKEN_EXPIRED);
            return;     // 필터 체인 중단
        } catch (Exception e) {
            sendErrorResponse(response, ErrorCode.ACCESS_TOKEN_REQUIRED);
        }
        // 7. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // 필터에서 직접 JSON 응답 반환 메서드
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");

        String json = String.format("{\"code\": \"%s\", \"message\": \"%s\"}",
                errorCode.name(), errorCode.getMessage());

        response.getWriter().write(json);
    }
}
