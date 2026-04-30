package com.ssak.ssak.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialLoginTokenResponse {
    private String accessToken;
    private String refreshToken;
    private Boolean isNewUser;
}
