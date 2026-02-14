package com.ssak.ssak.controller.user;

import com.ssak.ssak.domain.user.dto.*;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import com.ssak.ssak.service.user.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 일반 로그인을 수행합니다.
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> generalLogin(@RequestBody LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.generalLogin(request, response));
    }

    /**
     * 액세스 토큰을 재발급합니다.
     * @param refreshToken
     * @param response
     * @return
     */
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissueToken(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                      HttpServletResponse response) {
        // 쿠키가 존재하지 않는 경우
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return ResponseEntity.ok(authService.reissue(refreshToken, response));
    }

    /**
     * 일반 회원가입을 수행합니다.
     * @param request
     * @return
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    /**
     * 비밀번호를 재설정합니다.
     * @param request
     * @return
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    /**
     * 로그아웃을 수행합니다.
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(request, response));
    }

    //TODO: 회원탈퇴
    @PostMapping("/withdrawal")
    public ResponseEntity<String> withdrawal(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.withdrawal(request, response));
    }
}
