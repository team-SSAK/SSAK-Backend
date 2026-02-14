package com.ssak.ssak.security.OAuth;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "kakao":
                return new KakaoUserInfo(attributes);
            case "google":
                return new GoogleUserInfo(attributes);
            case "apple":
                return new AppleUserInfo(attributes);
            default:
                throw new IllegalArgumentException("지원하지 않는 로그인 방식입니다: " + registrationId);
        }
    }
}
