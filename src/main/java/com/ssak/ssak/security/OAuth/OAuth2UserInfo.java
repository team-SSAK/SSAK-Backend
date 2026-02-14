package com.ssak.ssak.security.OAuth;

import com.ssak.ssak.domain.user.LoginType;

public interface OAuth2UserInfo {
    String getProviderId();
    LoginType getProvider();
    String getEmail();
}
