package com.ssak.ssak.domain.coupon.dto;

import com.ssak.ssak.domain.coupon.Coupon;
import com.ssak.ssak.domain.coupon.CouponHist;
import com.ssak.ssak.domain.coupon.CouponStore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponHistResponse {
    private Long couponHistId;
    private Long couponId;
    private String couponNm;
    private String couponStore;
    private int couponPoint;
    private String couponImgUrl;
    private boolean couponWished;   // 쿠폰이 찜되어있는가

    public static CouponHistResponse from(CouponHist couponHist, boolean isWished) {
        return CouponHistResponse.builder()
                .couponHistId(couponHist.getCouponHistId())
                .couponId(couponHist.getCoupon().getCouponId())
                .couponNm(couponHist.getCoupon().getCouponName())
                .couponStore(couponHist.getCoupon().getCouponStore().getCouponStoreName())
                .couponPoint(couponHist.getCoupon().getCouponPoint())
                .couponImgUrl(couponHist.getCoupon().getCouponImgUrl())
                .couponWished(isWished)
                .build();
    }
}
