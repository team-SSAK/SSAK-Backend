package com.ssak.ssak.domain.util.dto;

import com.ssak.ssak.domain.util.EmailVerificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CodeVerificationRequest {
    private String email;

    @NotBlank(message = "인증코드를 입력해주세요")
    @Pattern(regexp = "^[0-9]{6}$", message = "6자리 숫자를 입력해주세요.")
    private String code;
    private EmailVerificationType type;
}