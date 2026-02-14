package com.ssak.ssak.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String code;
    private final String message;

    public static ErrorResponse from (ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }

    public static ErrorResponse of (String code, String message) {
        return new ErrorResponse(code, message);
    }
}
