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
    private String couponNm;
    private String couponStore;
    private int couponPoint;
    private String couponImgUrl;

    public static CouponHistResponse from(CouponHist couponHist) {
        return CouponHistResponse.builder()
                .couponHistId(couponHist.getCouponHistId())
                .couponNm(couponHist.getCoupon().getCouponName())
                .couponStore(couponHist.getCoupon().getCouponStore().getCouponStoreName())
                .couponPoint(couponHist.getCoupon().getCouponPoint())
                .couponImgUrl(couponHist.getCoupon().getCouponImgUrl())
                .build();
    }
}
