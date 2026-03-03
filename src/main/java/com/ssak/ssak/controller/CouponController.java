package com.ssak.ssak.controller;

import com.ssak.ssak.domain.coupon.CouponStatus;
import com.ssak.ssak.domain.coupon.dto.CouponHistResponse;
import com.ssak.ssak.domain.coupon.dto.CouponWishActionResponse;
import com.ssak.ssak.domain.coupon.dto.CouponWishRequest;
import com.ssak.ssak.domain.coupon.dto.CouponWishResponse;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    /**
     * 특정 사용자의 쿠폰 리스트(사용가능, 사용완료)를 조회한다.
     * @param userDetails
     * @param option
     * @return
     */
    @GetMapping("/my")
    public ResponseEntity<List<CouponHistResponse>> getMyCouponList(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                    @RequestParam CouponStatus option) {
        return ResponseEntity.ok(couponService.getMyCouponList(userDetails.getUserId(), option));
    }

    /**
     * 특정 사용자의 쿠폰 찜 목록을 조회한다.
     * @param userDetails
     * @return
     */
    @GetMapping("/my/wish")
    public ResponseEntity<List<CouponWishResponse>> getMyWishCouponList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(couponService.getMyWishCouponList(userDetails.getUserId()));
    }

    /**
     * 특정 사용자가 특정 쿠폰을 찜하거나 취소한다.
     * @param userDetails
     * @param request
     * @return
     */
    @PostMapping("/my/wish")
    public ResponseEntity<CouponWishActionResponse> updateCouponWishStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                           @RequestBody CouponWishRequest request) {
        return ResponseEntity.ok(couponService.updateCouponWishStatus(userDetails.getUserId(), request.getWishCouponId()));
    }

}
