package com.ssak.ssak.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 에러 소스
@Getter
public enum ErrorCode {
    // GENERAL
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "G001", "잘못된 요청입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "서버 오류가 발생했습니다"),

    // AUTH - USER
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "A001", "이미 사용 중인 이메일입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A002", "비밀번호가 올바르지 않습니다"),
    EMAIL_NOT_SENT(HttpStatus.BAD_REQUEST, "A003", "이메일 발송에 실패하였습니다"),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "A004", "이메일 인증이 완료되지않았습니다."),
    EMAIL_VERIFICATION_INVALID(HttpStatus.BAD_REQUEST, "A005", "인증코드가 올바르지 않거나 만료되었습니다"),
    SOCIAL_LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "A006", "소셜 로그인으로 가입된 계정입니다. 해당 소셜 로그인을 이용해주세요"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A007", "사용자를 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "A008","유효하지 않은 리프레시 토큰입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "A009", "액세스 토큰이 만료되었습니다."),
    ACCESS_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "A010", "액세스 토큰이 필요한 요청입니다."),
    INVALID_WD_REASON(HttpStatus.BAD_REQUEST, "A011", "존재하지 않는 사유입니다."),
    WD_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "A012", "사유 입력이 필요합니다."),
    LOGOUT_TOKEN(HttpStatus.BAD_REQUEST, "A013", "이미 로그아웃된 토큰입니다."),
    VERIFICATION_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "A014", "해당 계정에 대한 인증코드 발급 정보가 없습니다. 다시 전송해주세요."),

    // COUPON
    COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST, "C001", "존재하지 않는 쿠폰입니다."),
    INVALID_COUPON(HttpStatus.BAD_REQUEST, "C002", "유효하지 않은 쿠폰입니다."),
    INVALID_STORE_PASSWORD(HttpStatus.BAD_REQUEST, "C003", "쿠폰 가게의 비밀번호가 일치하지 않습니다."),

    // RESTAURANT
    RESTAURANT_NOT_FOUND(HttpStatus.BAD_REQUEST, "R001", "존재하지 않는 식당입니다."),

    // POINT
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "P001", "보유 포인트가 부족합니다."),

    // COMMUNITY_POST
    POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "CP001", "해당 게시글을 찾을 수 없습니다."),
    NOT_POST_OWNER(HttpStatus.BAD_REQUEST, "CP002", "본인이 작성한 게시물만 수정 및 삭제할 수 있습니다."),
    ALREADY_REPORTED(HttpStatus.BAD_REQUEST, "CP003", "이미 신고되었습니다."),

    // COMMUNITY_COMMENT
    COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "CC001", "해당 댓글을 찾을 수 없습니다."),
    NOT_COMMENT_OWNER(HttpStatus.BAD_REQUEST, "CC002", "본인이 작성한 댓글만 수정 및 삭제할 수 있습니다."),

    // S3
    IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "S001", "이미지가 존재하지 않습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S003", "이미지를 업로드하는 도중 에러가 발생하였습니다."),
    IMAGE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "이미지를 삭제하는 도중 에러가 발생하였습니다."),

    // NOTICE
    NOTICE_NOT_FOUND(HttpStatus.BAD_REQUEST, "N001", "해당 공지사항을 찾을 수 없습니다."),

    // AI RELATED
    INCORRECT_RESPONSE(HttpStatus.BAD_REQUEST, "M001", "AI 서버로 부터 올바르지 않은 응답을 받았습니다."),
    INCORRECT_IMAGE(HttpStatus.BAD_REQUEST, "M002", "잔반 이미지가 존재하지 않거나 올바르지 않습니다");

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
