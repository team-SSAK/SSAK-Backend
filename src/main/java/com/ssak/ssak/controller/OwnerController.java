package com.ssak.ssak.controller;

import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.domain.user.UserRole;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/owner")
@RequiredArgsConstructor
public class OwnerController {
    private final UserRepository userRepository;

    @PostMapping("/approve/{userId}")
    public ResponseEntity<String> approveOwner(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (user.getUserRole() != UserRole.OWNER) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        user.approveOwner();
        userRepository.save(user);
        return ResponseEntity.ok("사장님 계정이 승인되었습니다.");
    }
}
