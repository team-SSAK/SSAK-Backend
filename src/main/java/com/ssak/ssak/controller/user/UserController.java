package com.ssak.ssak.controller.user;

import com.ssak.ssak.domain.Point.dto.PointHistResponse;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.dto.*;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 현재 로그인한 사용자 정보 조회
     * @param userDetails
     * @return
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        // SecurityContext에서 인증된 사용자 정보를 가져옴
        User user = userDetails.getUser();

        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * 사용자 세부정보 수정
     * @param userDetails
     * @param request
     * @return
     */
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> modifyUser(@AuthenticationPrincipal CustomUserDetails userDetails, @ModelAttribute UserModifyRequest request) throws IOException {
        return ResponseEntity.ok(userService.modifyUser(userDetails.getUserId(), request));
    }

    /**
     * 탈퇴 사유 목록 조회
     * @return List<WithdrawalReasonMstrResponse>
     */
    @GetMapping("/withdrawal")
    public ResponseEntity<List<WithdrawalReasonMstrResponse>> getWithdrawalReasonResponseList() {
        return ResponseEntity.ok(userService.getWithdrawalReasonResponseList());
    }

    /**
     * 탈퇴 사유 저장
     * @param request
     * @return void
     */
    @PostMapping("/withdrawal")
    public ResponseEntity<Void> saveWithdrawalReason(@RequestBody WithdrawalReasonRequest request) {
        userService.saveWithdrawalReason(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 사용자의 알림 설정 여부 조회
     * @param userDetails
     * @return
     */
    @GetMapping("/notification")
    public ResponseEntity<NotificationResponse> getNotification(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getNotification(userDetails.getUser().getUserId()));
    }

    /**
     * 특정 사용자의 알림 설정 변경
     * @param userDetails
     * @param request
     * @return
     */
    @PatchMapping("/notification")
    public ResponseEntity<NotificationResponse> updateNotification(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @RequestBody UpdateNotificationRequest request) {
        return ResponseEntity.ok(userService.updateNotificationYN(userDetails.getUserId(), request));
    }

    /**
     * 처음 가입한 사용자의 정보 수정
     * @param userDetails
     * @param request
     * @return
     */
    @PostMapping("/create-details")
    public ResponseEntity<CreateDetailResponse> createDetails(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody CreateDetailRequest request) {
        return ResponseEntity.ok(userService.createDetails(userDetails.getUserId(), request));
    }

}
