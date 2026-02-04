package com.ssak.ssak.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 에러 소스
@Getter
public enum ErrorCode {
    // COMMON
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 오류가 발생했습니다"),

    // AUTH - USER
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "A001", "이미 사용 중인 이메일입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A002", "비밀번호가 올바르지 않습니다"),
    EMAIL_NOT_SENT(HttpStatus.BAD_REQUEST, "A003", "이메일 발송에 실패하였습니다"),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "A004", "이메일 인증이 완료되지않았습니다."),
    EMAIL_VERIFICATION_INVALID(HttpStatus.BAD_REQUEST, "A005", "인증코드가 올바르지 않거나 만료되었습니다"),
    SOCIAL_LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "A006", "소셜 로그인으로 가입된 계정입니다. 해당 소셜 로그인을 이용해주세요"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A007", "가입되지 않은 사용자입니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
