package com.ssak.ssak.security.OAuth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssak.ssak.domain.user.SignupStatus;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.security.CustomUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// 소셜 로그인용

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Value("${apple.team-id}")
    private String appleTeamId;

    @Value("${apple.client.id}") // Service Id
    private String appleClientId;

    @Value("${apple.key-id}")
    private String appleKeyId;

    @Value("${apple.key-path}")
    private String appleKeyPath;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 에러 발생", e);
            throw new OAuth2AuthenticationException(e.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("이메일을 찾을 수 없습니다.");
        }

        Optional<User> optionalUser = userRepository.findByUserEmail(oAuth2UserInfo.getEmail());

        // 신규 회원 여부 판단
        boolean isNew = optionalUser.isEmpty();
        boolean isPendingUser = optionalUser.isPresent() && optionalUser.get().getSignupStatus() == SignupStatus.PENDING;

        boolean isNewUser = isNew || isPendingUser;

        User user = optionalUser.orElseGet(() -> createUser(oAuth2UserInfo));

        // 기존사용자 여부 임시저장
        redisTemplate.opsForValue().set(
                "IS_NEW_USER:" + user.getUserEmail(),
                isNewUser,
                1,
                TimeUnit.MINUTES
        );

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }

    private User createUser(OAuth2UserInfo oAuth2UserInfo) {
        User newUser = User.builder()
                .userEmail(oAuth2UserInfo.getEmail())
                .userNm("사용자" + System.currentTimeMillis())
                .loginType(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getProviderId())
                .userPw(null)
                .build();

        return userRepository.save(newUser);
    }


    OAuth2User processAppleLogin(String code, String userJson) {
        // 1. Client Secret 생성
        String clientSecret = createClientSecret();

        // 2. 애플 토큰 발급 API 호출하여 ID Token 가져오기
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://appleid.apple.com/auth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", appleClientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (!jsonNode.has("id_token")) {
                throw new RuntimeException("Apple 토큰 발급 실패: ID Token이 없습니다.");
            }

            String idToken = jsonNode.get("id_token").asText();

            // 3. ID Token을 디코딩하여 sub(고유 ID) 및 이메일 추출
            String[] parts = idToken.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payloadNode = objectMapper.readTree(payload);

            String appleSub = payloadNode.get("sub").asText();
            String email = payloadNode.has("email") ? payloadNode.get("email").asText() : null;

            // 4. AppleUserInfo 객체 생성
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", appleSub);
            attributes.put("email", email);

            OAuth2UserInfo oAuth2UserInfo = new AppleUserInfo(attributes);

            // 5. 기존 로그인 흐름과 동일한 로직 적용
            Optional<User> optionalUser = userRepository.findByUserEmail(oAuth2UserInfo.getEmail());

            // 신규 회원 여부 판단
            boolean isNew = optionalUser.isEmpty();
            boolean isPendingUser = optionalUser.isPresent() && optionalUser.get().getSignupStatus() == SignupStatus.PENDING;

            boolean isNewUser = isNew || isPendingUser;

            User user = optionalUser.orElseGet(() -> createUser(oAuth2UserInfo));

            // 기존사용자 여부 임시저장
            redisTemplate.opsForValue().set(
                    "IS_NEW_USER:" + user.getUserEmail(),
                    isNewUser,
                    1,
                    TimeUnit.MINUTES
            );

            // 6. CustomUserDetails 생성하여 반환 (Attributes 추가)
            return new CustomUserDetails(user, attributes);

        } catch (Exception e) {
            throw new RuntimeException("Apple OAuth 토큰 교환 실패", e);
        }
    }


    private String createClientSecret() {
        try {
            String cleanPath = appleKeyPath.startsWith("file:") ? appleKeyPath.substring(5) : appleKeyPath;
            Resource resource = new FileSystemResource(cleanPath);
            String keyContent;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                keyContent = reader.lines().collect(Collectors.joining("\n"));
            }

            // 2. PEM 형식의 헤더, 푸터 및 공백/줄바꿈 제거
            String cleanKey = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // 3. Base64 디코딩
            byte[] decodeKey = Base64.getDecoder().decode(cleanKey);

            // 4. PrivateKey 생성
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodeKey);
            KeyFactory kf = KeyFactory.getInstance("EC");
            PrivateKey privateKey = kf.generatePrivate(keySpec);

            Date now = new Date();
            Date expiration = new Date(now.getTime() + 3600000); // 1시간 유효

            return Jwts.builder()
                    .setHeaderParam("kid", appleKeyId)
                    .setHeaderParam("alg", "ES256")
                    .setIssuer(appleTeamId)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .setAudience("https://appleid.apple.com")
                    .setSubject(appleClientId)
                    .signWith(privateKey, SignatureAlgorithm.ES256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Apple Client Secret 생성 실패", e);
        }
    }
}
