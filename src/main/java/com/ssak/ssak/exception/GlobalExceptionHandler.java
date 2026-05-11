package com.ssak.ssak.exception;

import com.ssak.ssak.service.SlackNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    private final SlackNotificationService slackNotificationService;

    // 1. 비즈니스 예외 : 서비스가 의도적으로 던진 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ErrorResponse.from(errorCode));
    }

    // 2. 검증 예외 : @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("C_VALID", e.getBindingResult().getFieldError().getDefaultMessage()));
    }

    // 3. 시스템 에러 (나머지)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        // 개발용 로그 출력
        e.printStackTrace();
        log.error("Unhandled Exception: ", e.getMessage());

        // 슬랙 알림 발송
        String method = request.getMethod();
        String url = request.getRequestURI();
        slackNotificationService.sendError(e, method, url);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
