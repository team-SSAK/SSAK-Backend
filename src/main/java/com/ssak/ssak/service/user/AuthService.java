package com.ssak.ssak.service.user;

import com.ssak.ssak.domain.Point.PointHistRepository;
import com.ssak.ssak.domain.community.CommentRepository;
import com.ssak.ssak.domain.community.PostRepository;
import com.ssak.ssak.domain.coupon.CouponHistRepository;
import com.ssak.ssak.domain.coupon.CouponWishRepository;
import com.ssak.ssak.domain.restaurant.RestaurantWishRepository;
import com.ssak.ssak.domain.user.*;
import com.ssak.ssak.domain.user.dto.*;
import com.ssak.ssak.domain.util.EmailVerificationType;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import com.ssak.ssak.security.JWT.JwtTokenProvider;
import com.ssak.ssak.service.util.EmailVerificationService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final NotificationRepository notificationRepository;
    private final PointHistRepository pointHistRepository;
    private final CouponHistRepository couponHistRepository;
    private final CouponWishRepository couponWishRepository;
    private final RestaurantWishRepository restaurantWishRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${kakao.client.id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    /**
     * 일반 회원가입을 수행합니다.
     * @param request
     * @return
     */
    @Transactional
    public UserResponse signUp(SignUpRequest request) {

        // 1. 이메일 중복 체크
        if(userRepository.existsByUserEmail(request.getUserEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 이메일 인증 여부 확인
        if (!emailVerificationService.isVerified(request.getUserEmail(), EmailVerificationType.SIGNUP)) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getUserPw());

        // 4. 사용자 생성
        User user = User.builder()
                .userEmail(request.getUserEmail())
                .userPw(encodedPassword)
                .userNm(request.getUserNm())
                .loginType(LoginType.NORMAL)
                .build();

        // 5. 데이터베이스에 저장
        User savedUser = userRepository.save(user);

        // 6. 사용자 마케팅 수신 여부 notification 테이블에 저장
        Notification savedNotification = Notification.builder()
                .user(savedUser)
                .communityNotiYn(true)
                .eventNotiYn(request.isMarketingAgreeYn())
                .nightNotiYn(request.isMarketingAgreeYn())
                .build();
        notificationRepository.save(savedNotification);

        // 7. 인증정보 Redis에서 제거
        emailVerificationService.clearVerification(request.getUserEmail(), EmailVerificationType.SIGNUP);

        return UserResponse.from(savedUser);
    }

    /**
     * 일반 로그인을 수행합니다.
     * @param request
     * @param response
     * @return
     */
    @Transactional
    public TokenResponse generalLogin(LoginRequest request, HttpServletResponse response) {
        // 1. 사용자 조회
        User user = userRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 일반 로그인 사용자 인지 확인
        if(user.getLoginType() != LoginType.NORMAL) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_REQUIRED);
        }

        // 3. 비밀번호 검증
        if(!passwordEncoder.matches(request.getUserPw(), user.getUserPw())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        log.debug("비밀번호 검증 완료");
        log.debug(user.getUserEmail());

        // 4. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserEmail());

        // 5. Redis에 RefreshToken 저장(기존 토큰 덮어쓰기)
        //Key : "RT:" + 이메일, Value: RefreshToken 값, TTL:14일
        redisTemplate.opsForValue().set(
                "RT:" + user.getUserEmail(),
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * AccessToken을 재발급합니다.
     * @param refreshToken
     * @return
     */
    public TokenResponse reissue(String refreshToken) {
        // 1. RefreshToken 검증
        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        // 2. 토큰에서 이메일 추출
        String email = jwtTokenProvider.getEmail(refreshToken);

        // 3. Redis에서 해당 이메일의 Refresh Token 가져오기
        String savedRefreshToken = (String) redisTemplate.opsForValue().get("RT:" + email);

        // 4. 요청 받은 토큰과 Redis 토큰 비교
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 5. 새로운 accessToken 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        // 6. Redis 업데이트
        redisTemplate.opsForValue().set(
                "RT:" + email,
                newRefreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    /**
     * 비밀번호를 재설정합니다.
     * @param request
     * @return
     */
    @Transactional
    public String resetPassword(PasswordResetRequest request) {
        // 1. 이메일 인증여부 확인
        if (!emailVerificationService.isVerified(request.getEmail(), EmailVerificationType.PASSWORD_RESET)) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 2. 사용자 조회
        User user = userRepository.findByUserEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 비밀번호 암호화 및 변경
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedPassword);

        // 4. Redis 인증정보 삭제
        emailVerificationService.clearVerification(request.getEmail(), EmailVerificationType.PASSWORD_RESET);

        return "비밀번호가 재설정되었습니다.";
    }

    /**
     * 로그아웃을 수행합니다.
     * @param request
     * @param response
     * @return
     */
    @Transactional
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 헤더에서 AccessToken 추출
        String accessToken = jwtTokenProvider.resolveToken(request);

        // 토큰이 아예 없는 경우
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_REQUIRED);
        }

        try{
            // 1. 토큰 유효성 검사
            if(jwtTokenProvider.validateToken(accessToken)) {
                String email =  jwtTokenProvider.getEmail(accessToken);
                Long expiration = jwtTokenProvider.getExpiration(accessToken);

                // 2. Redis에서 해당 유저의 RefreshToken 삭제
                redisTemplate.delete("RT:" + email);

                // 3. AccessToken 블랙리스트 추가
                redisTemplate.opsForValue().set(
                        "BL:" + accessToken,
                        "logout",
                        expiration,
                        TimeUnit.MILLISECONDS
                );
                log.info("Access Token 블랙리스트 등록 완료");
            } else {
                // 토큰이 유효하지 않은 경우
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        }

        // 4. 소셜 로그인한 사용자인 경우
        User user = userRepository.findByUserEmail(jwtTokenProvider.getEmail(accessToken)).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(user.getLoginType() == LoginType.KAKAO){ // 카카오 로그인
            return "https://kauth.kakao.com/oauth/logout"
                    + "?client_id=" + kakaoClientId
                    + "&logout_redirect_uri=" + kakaoRedirectUri;
        } else { // * 구글은 따로 로그아웃 기능 제공 X
            return "성공적으로 로그아웃되었습니다.";
        }
    }

    /**
     * 회원탈퇴를 수행합니다.
     * @param request
     * @param response
     * @return
     */
    @Transactional
    public String withdrawal(HttpServletRequest request, HttpServletResponse response) {
        // 1. 헤더에서 AccessToken 추출
        String accessToken = jwtTokenProvider.resolveToken(request);

        // 토큰이 없는 경우 / 올바른 토큰이 아닌 경우
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_REQUIRED);
        } else if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);  // INVALID_TOKEN
        }

        String email = jwtTokenProvider.getEmail(accessToken);
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 소셜 로그인 연동 해제 (Unlink)
        if (user.getLoginType() == LoginType.KAKAO){
            unlinkKakao(user.getProviderId());
        }
        // * 구글은 프론트 측에서 처리하는 것이 더 효율적인 것 같음

        // 3. 연관 데이터 삭제 TODO 이부분 추가된 내용 연관관계 관련
        pointHistRepository.deleteAllByUser(user);
        couponHistRepository.deleteAllByUser(user);
        couponWishRepository.deleteAllByUser(user);
        notificationRepository.deleteAllByUser(user);
        restaurantWishRepository.deleteAllByUser(user);

        // 4. 사용자가 등록했던 게시물, 댓글 연관관계 제거
        postRepository.findByUser(user).forEach(post -> {
            post.removeUser();
        });

        commentRepository.findByUser(user).forEach(comment -> {
            comment.removeUser();
        });

        // 5. 리프레시 토큰 삭제
        redisTemplate.delete("RT:" + email);

        // 6. 유저 삭제
        userRepository.deleteById(user.getUserId());

        return "회원 탈퇴가 완료되었습니다.";
    }

    /**
     * 카카오 unlink를 수행합니다.
     * @param providerId
     */
    private void unlinkKakao(String providerId) {
        String url = "https://kapi.kakao.com/v1/user/unlink";

        // 1. 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);

        // 2. 파라미터 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", providerId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            log.error("카카오 연동 해제 실패 (providerId: {}): {}", providerId, e.getMessage());
        }
    }


    /**
     * 소셜 로그인 oauth2 과정에서 code -> token 변환
     * @param request
     * @return
     */
    @Transactional
    public TokenResponse exchangeToken(TokenRequest request) {
        // 1. Redis에서 코드로 이메일 조회
        String email = (String) redisTemplate.opsForValue().get("OAUTH_CODE:" + request.getCode());

        if (email == null || email.isEmpty()) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_INVALID);
        }

        // 2. 코드 즉시 삭제 (1회용)
        redisTemplate.delete("OAUTH_CODE:" + request.getCode());

        // 3. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        // 4. Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set(
                "RT:" + email,
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        // 5. JSON 응답
        return new TokenResponse(accessToken, refreshToken);
    }
}