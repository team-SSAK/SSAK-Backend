package com.ssak.ssak.service.user;

import com.ssak.ssak.domain.user.*;
import com.ssak.ssak.domain.user.dto.*;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final WithdrawalReasonMSTRRepository withdrawalReasonMSTRRepository;
    private final WithdrawalReasonHistRepository withdrawalReasonHistRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 사용자 정보를 수정한다.
     * @param userId
     * @param request
     * @return
     */
    @Transactional
    public UserResponse modifyUser(Long userId, UserModifyRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 정보 수정
        user.modifyProfile(request.getNickname());

        return UserResponse.from(user);
    }

    /**
     * 탈퇴 사유 목록을 조회한다.
     * @return List<WithdrawalReasonMstrResponse>
     */
    @Transactional(readOnly = true)
    public List<WithdrawalReasonMstrResponse> getWithdrawalReasonResponseList() {
        // 1. DB에서 Entity 리스트 조회
        List<WithdrawalReasonMSTR> reasonList = withdrawalReasonMSTRRepository.findAll();
        // 2. Entity를 DTO로 변환
        return reasonList.stream()
                .map(reason -> new WithdrawalReasonMstrResponse(
                        String.valueOf(reason.getWithdrawalReasonId()),
                        reason.getWithdrawalReasonContent()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 탈퇴 사유를 저장한다.
     * @param request WithdrawalReasonRequest
     */
    @Transactional
    public void saveWithdrawalReason(WithdrawalReasonRequest request) {
        // 1. 선택한 사유 마스터 테이블에서 찾기
        WithdrawalReasonMSTR wdReason = withdrawalReasonMSTRRepository.findById(Long.valueOf(request.getSelectedWdReasonId()))
                .orElseThrow(() -> new CustomException((ErrorCode.INVALID_WD_REASON)));
        // 2. '기타' 사유인데 사유 입력이 안 되어 있는 경우
        if (request.getReason() == null || request.getReason().isEmpty()) {
            throw new CustomException(ErrorCode.WD_REASON_REQUIRED);
        }

        // 3. 엔티티 생성 및 저장
        WithdrawalReasonHist response = WithdrawalReasonHist.builder()
                .withdrawalReasonMSTR(wdReason)
                .withdrawalReasonDetail(request.getReason())
                .build();
        withdrawalReasonHistRepository.save(response);
    }

    /**
     * 사용자의 알림 설정을 조회한다.
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long userId) {
        Notification noti = notificationRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        return NotificationResponse.from(noti);
    }

    /**
     * 사용자의 알림 설정을 변경한다.
     * @param userId
     * @param request
     * @return
     */
    @Transactional
    public NotificationResponse updateNotificationYN(Long userId, UpdateNotificationRequest request) {
        Notification noti = notificationRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        noti.updateNotification(
            request.isEventNotiYn(), request.isCommunityYn(), request.isNightYn()
        );
        return NotificationResponse.from(noti);
    }
}