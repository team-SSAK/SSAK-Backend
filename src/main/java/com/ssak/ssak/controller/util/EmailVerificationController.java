package com.ssak.ssak.controller.util;

import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.domain.util.EmailVerificationType;
import com.ssak.ssak.domain.util.dto.CodeVerificationRequest;
import com.ssak.ssak.domain.util.dto.EmailVerificationRequest;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import com.ssak.ssak.service.util.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;

    // 인증 코드 발송
    @PostMapping("/send")
    public ResponseEntity<?> sendCode(@RequestBody @Valid EmailVerificationRequest request) {
        try {
            log.info("인증 코드 발송 요청 - 이메일: {}, 타입: {}", request.getEmail(), request.getType());

            // 1. 비밀번호 재설정인 경우
            if (request.getType() == EmailVerificationType.PASSWORD_RESET) {
                // 1-a. 가입된 이메일인지 확인 필요
                User user = userRepository.findByUserEmail(request.getEmail())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                // 1-b. 일반 로그인 사용자가 맞는지 확인 필요
                if(user.getProviderId() != null) {
                    throw new CustomException(ErrorCode.SOCIAL_LOGIN_REQUIRED);
                }
            }
            // 2. 회원가입인 경우 중복 검증
            if(request.getType() == EmailVerificationType.SIGNUP) {
                if(userRepository.existsByUserEmail(request.getEmail())) {
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
                }
            }
            emailVerificationService.sendVerificationCode(
                    request.getEmail(),
                    request.getType()
            );
            return ResponseEntity.ok("인증코드가 발송되었습니다.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(ErrorCode.EMAIL_NOT_SENT);
        }
    }

    // 인증 코드 검증
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody CodeVerificationRequest request) {
        boolean isValid = emailVerificationService.verifyCode(
                request.getEmail(),
                request.getCode(),
                request.getType()
        );

        if (isValid) {
            return ResponseEntity.ok("인증 성공");
        }
        throw new CustomException(ErrorCode.EMAIL_VERIFICATION_INVALID);
    }
}
