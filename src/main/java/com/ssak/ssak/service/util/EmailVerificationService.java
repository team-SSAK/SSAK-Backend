package com.ssak.ssak.service.util;

import com.ssak.ssak.domain.util.EmailVerificationType;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private static final int CODE_EXPIRATION_MINUTES = 5;  // 인증코드 유효시간
    private static final int VERIFIED_EXPIRATION_MINUTES = 30; // 인증완료 유효시간

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String senderEmail;

    // 인증 코드 발송
    public void sendVerificationCode(String email, EmailVerificationType type) {
        // 1. 6자리 랜덤 코드 생성
        String code = generateCode();

        // 2. Redis 저장 (TTL)
        String key = "verification:" + type.name() + ":" + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        // 3. 이메일 발송
        log.info("인증코드 발송 - 이메일: {}, 코드: {}", email, code);
        sendEmail(email, code, type);
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code, EmailVerificationType type) {
        String key = "verification:" + type.name() + ":" + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null || !savedCode.equals(code)) {
            log.warn("인증코드 불일치 - 이메일: {}", email);
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_INVALID);
        }
        // 검증 성공 시 코드 삭제 및 인증 완료 플래그 Redis 저장
        redisTemplate.delete(key);
        redisTemplate.opsForValue().set("verified:" + type.name() + ":" + email, "true", VERIFIED_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        log.info("인증 성공 - 이메일: {}", email);
        return true;
    }

    // 인증 완료 여부 확인
    public boolean isVerified(String email, EmailVerificationType type) {
        String key = "verified:" + type.name() + ":" + email;
        return redisTemplate.hasKey(key);
    }

    // 인증 완료 후 Redis 플래그 삭제 (회원 가입 완료 시)
    public void clearVerification(String email, EmailVerificationType type) {
        String key = "verification:" + type + ":" + email;
        redisTemplate.delete(key);
        log.info("인증 정보 삭제 - 이메일: {}", email);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void sendEmail(String to, String code, EmailVerificationType type) {
        String subject = (type==EmailVerificationType.SIGNUP)? "회원가입 인증코드" : "비밀번호 재설정 인증 코드";
        String text = String.format(
                "인증코드: %s\n\n5분 내에 입력해주세요.",
                code
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(senderEmail);

        mailSender.send(message);
    }
}
