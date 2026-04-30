package com.ssak.ssak.controller;

import com.ssak.ssak.domain.coupon.CouponStatus;
import com.ssak.ssak.domain.coupon.CouponType;
import com.ssak.ssak.domain.coupon.dto.*;
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

    /**
     * 쿠폰 목록을 조회한다.
     * @param type
     * @return
     */
    @GetMapping
    public ResponseEntity<List<CouponListResponse>> getCouponList(@RequestParam(required = false) CouponType type) {
        return ResponseEntity.ok(couponService.getCouponList(type));
    }

    /**
     * 쿠폰 상세정보를 조회한다.
     * @param couponId
     * @return
     */
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponResponse> getCouponDetail(@PathVariable Long couponId) {
        return ResponseEntity.ok(couponService.getCouponDetail(couponId));
    }

    @PostMapping("/exchange")
    public ResponseEntity<CouponExchangeResponse> exchangeIntoCoupon(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                     @RequestBody CouponExchangeRequest request) {
        return ResponseEntity.ok(couponService.exchangeIntoCoupon(userDetails.getUserId(), request.getExchangeCouponId()));
    }


    @PostMapping("/use")
    public ResponseEntity<CouponUseResponse> useCoupon(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @RequestBody CouponUseRequest request) {
        return ResponseEntity.ok(couponService.useCoupon(userDetails.getUserId(), request));
    }
}
