package com.ssak.ssak.controller.user;

import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.dto.UserResponse;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        // SecurityContext에서 인증된 사용자 정보를 가져옴
        User user = userDetails.getUser();

        return ResponseEntity.ok(new UserResponse(user));
    }
}
