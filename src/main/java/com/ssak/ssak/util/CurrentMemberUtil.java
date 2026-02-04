package com.ssak.ssak.util;

import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// 현재 로그인한 사용자 정보 반환하는 유틸
@Component
public class CurrentMemberUtil {
    public CustomUserDetails getCurrentUserDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        return null;
    }

    public User getCurrentUser() {
        CustomUserDetails user = getCurrentUserDetails();
        return (user != null)? user.getUser() : null;
    }
}
