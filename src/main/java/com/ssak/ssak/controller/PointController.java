package com.ssak.ssak.controller;

import com.ssak.ssak.domain.Point.dto.PointCurrentResponse;
import com.ssak.ssak.domain.Point.dto.PointHistResponse;
import com.ssak.ssak.domain.Point.PointType;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 로그인한 사용자의 포인트 히스토리를 반환합니다.
     * @param userDetails
     * @return
     */
    @GetMapping("/users/point")
    public ResponseEntity<List<PointHistResponse>> getUserPointHistory(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(required = false) PointType option) {
        return ResponseEntity.ok(pointService.getMyPointHist(userDetails.getUserId(), option));
    }

    /**
     * 사용자의 현재 포인트를 반환합니다.
     * @param userDetails
     * @return
     */
    @GetMapping("/users/current-point")
    public ResponseEntity<PointCurrentResponse> getUserCurrentPoint(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(pointService.getUserCurrentPoint(userDetails.getUserId()));
    }
}
