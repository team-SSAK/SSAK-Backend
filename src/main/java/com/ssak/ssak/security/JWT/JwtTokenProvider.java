package com.ssak.ssak.security.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;       // 비밀키
    @Value("${jwt.expiration}")
    private Long expiration;        // 토큰 유효시간

    // 최신 jjwt 라이브러리는 String 비밀키를 java.security,Key 객체로 변환해서 넣어주어야 함
    private Key signingKey;

    // Bean 생성 후 키를 변환하는 과정 (String -> Key)
    @PostConstruct
    public void init() {
        // 비밀키를 바이트 배열로 변환하여 Key 객체 생성
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 생성
    public String generateToken(String email) {
        // Claims: JWT의 payload에 들어갈 정보
        Claims claims = Jwts.claims().setSubject(email);    //subject - 사용자 이메일

        Date now = new Date();
        Date validity = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)  //데이터
                .setIssuedAt(now)   //토큰 발행 시간
                .setExpiration(validity)  //만료시간
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 이메일 추출
    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검증
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder()
//                .setSigningKey(signingKey)
//                .build()
//                .parseClaimsJws(token);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            System.out.println(">>> 잘못된 JWT 서명입니다.");
            e.printStackTrace(); // ★ 에러 로그 출력
        } catch (ExpiredJwtException e) {
            System.out.println(">>> 만료된 JWT 토큰입니다.");
            e.printStackTrace(); // ★ 에러 로그 출력
        } catch (UnsupportedJwtException e) {
            System.out.println(">>> 지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println(">>> JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // HTTP 요청 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}