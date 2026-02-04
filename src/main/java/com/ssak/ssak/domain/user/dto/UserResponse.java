package com.ssak.ssak.domain.user.dto;

import com.ssak.ssak.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userEmail;
    private String userNm;
    private boolean marketingAgreeYn;

    public UserResponse(User user) {
        this.userEmail = user.getUserEmail();
        this.userNm = user.getUserNm();
        this.marketingAgreeYn = user.isMarketingAgreeYn();
    }
}