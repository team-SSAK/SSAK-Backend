package com.ssak.ssak.security.OAuth;

import com.ssak.ssak.domain.user.LoginType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class AppleUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public LoginType getProvider() {
        return LoginType.APPLE;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
}
