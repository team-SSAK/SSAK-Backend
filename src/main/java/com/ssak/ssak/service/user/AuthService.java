package com.ssak.ssak.service.user;

import com.ssak.ssak.domain.user.LoginType;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.domain.user.dto.*;
import com.ssak.ssak.domain.util.EmailVerificationType;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import com.ssak.ssak.security.JWT.JwtTokenProvider;
import com.ssak.ssak.service.util.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입 (일반)
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
                .marketingAgreeYn(request.isMarketingAgreeYn())
                .build();

        // 5. 데이터베이스에 저장
        User savedUser = userRepository.save(user);

        // 6. 인증정보 Redis에서 제거
        emailVerificationService.clearVerification(request.getUserEmail(), EmailVerificationType.SIGNUP);

        return new UserResponse(savedUser);
    }

    // 로그인
    @Transactional(readOnly = true)
    public TokenResponse generalLogin(LoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 일반 로그인 사용자 인지 확인
        if(user.getLoginType() != LoginType.NORMAL) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_REQUIRED);
        }

        //TODO: 이메일 인증 미완료시
        //if(!user.isEmailVerified()) {
        //  throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED
        //}

        // 3. 비밀번호 검증
        if(!passwordEncoder.matches(request.getUserPw(), user.getUserPw())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        log.debug("비밀번호 검증 완료");
        log.debug(user.getUserEmail());
        // 4. JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getUserEmail());
        //TODO : access, refresh 따로 생성
        //TODO: RefreshToken 저장 (기존 토큰 삭제 후)

        return new TokenResponse(token);
    }

    //TODO: 토큰 갱신


    // 비밀번호 재설정
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
}
