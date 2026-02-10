package com.ssak.ssak.controller.user;

import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.dto.UserModifyRequest;
import com.ssak.ssak.domain.user.dto.UserResponse;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        // SecurityContext에서 인증된 사용자 정보를 가져옴
        User user = userDetails.getUser();

        return ResponseEntity.ok(UserResponse.from(user));
    }

    // 사용자 세부정보 수정
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> modifyUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserModifyRequest request) {
        return ResponseEntity.ok(userService.modifyUser(userDetails.getUserId(), request));
    }
}
