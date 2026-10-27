package com.ssak.ssak.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OwnerSignupRequest {
    private String userEmail;
    private String userPw;
    private String userNm;
    private Long restaurantId;
    private boolean marketingAgreeYn;
}
