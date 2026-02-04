package com.ssak.ssak.security;

import com.ssak.ssak.domain.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {
    private final User user;
    private Map<String, Object> attributes; // for OAuth2 login

    // 일반 로그인용 생성자
    public CustomUserDetails(User user) {
        this.user = user;
    }

    // OAuth2 로그인용 생성자
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // ========= UserDetails 구현 ==========

    // 권한
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()  {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getUserPw();
    }

    @Override
    public String getUsername() {
        // Spring Security는 username으로 식별하니까 email 반환
        return user.getUserEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // =========== OAuth2User 구현 =========
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        // 소셜 로그인한 사람의 '고유 식별자'를 리턴해야함 -> providerId말고 그냥 우리 DB의 PK 반환하도록 구현
        return String.valueOf(user.getUserId());
    }
}
