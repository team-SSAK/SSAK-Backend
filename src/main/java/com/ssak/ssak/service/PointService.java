package com.ssak.ssak.service;

import com.ssak.ssak.domain.Point.PointHist;
import com.ssak.ssak.domain.Point.PointHistRepository;
import com.ssak.ssak.domain.Point.dto.PointCurrentResponse;
import com.ssak.ssak.domain.Point.dto.PointHistResponse;
import com.ssak.ssak.domain.Point.dto.PointType;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointHistRepository pointHistRepository;
    private final UserRepository userRepository;

    /**
     * 특정 사용자의 포인트 내역 조회
     * @param userId
     * @return
     */
    public List<PointHistResponse> getMyPointHist(Long userId, PointType option) {
        List<PointHist> pointHists = (option == null)
                ? pointHistRepository.findAllByUser_UserIdOrderByCreatedAtDesc(userId)
                : pointHistRepository.findAllByUser_UserIdAndPointTypeOrderByCreatedAtDesc(userId, option);
        return pointHists.stream()
                .map(PointHistResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 현재 보유 포인트를 조회합니다.
     * @param userId
     * @return
     */
    public PointCurrentResponse getUserCurrentPoint(Long userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));
        return PointCurrentResponse.from(user);
    }
}
