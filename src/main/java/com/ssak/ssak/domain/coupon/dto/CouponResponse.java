package com.ssak.ssak.domain.coupon.dto;

import com.ssak.ssak.domain.coupon.Coupon;
import com.ssak.ssak.domain.coupon.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private Long couponId;
    private String couponName;
    private String couponDescription;
    private int couponPoint;
    private CouponType couponType;
    private String couponStore;
    private String couponImgUrl;
    private int couponValidTerm;

    public static CouponResponse from (Coupon coupon) {
        return CouponResponse.builder()
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .couponDescription(coupon.getCouponDesc())
                .couponPoint(coupon.getCouponPoint())
                .couponType(coupon.getCouponType())
                .couponStore(coupon.getCouponStore().getCouponStoreName())
                .couponImgUrl(coupon.getCouponImgUrl())
                .couponValidTerm(coupon.getCouponValidTerm())
                .build();
    }
}
