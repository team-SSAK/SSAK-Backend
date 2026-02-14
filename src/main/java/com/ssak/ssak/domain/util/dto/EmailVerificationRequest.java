package com.ssak.ssak.domain.util.dto;

import com.ssak.ssak.domain.util.EmailVerificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailVerificationRequest {
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotNull(message = "타입은 필수입니다")
    private EmailVerificationType type;
}
