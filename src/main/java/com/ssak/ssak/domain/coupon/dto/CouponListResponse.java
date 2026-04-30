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
public class CouponListResponse {
    private String couponImgUrl;
    private String couponName;
    private int couponPoint;
    private CouponType couponType;
    private String couponStore;

    public static CouponListResponse from(Coupon coupon) {
        return CouponListResponse.builder()
                .couponImgUrl(coupon.getCouponImgUrl())
                .couponName(coupon.getCouponName())
                .couponPoint(coupon.getCouponPoint())
                .couponType(coupon.getCouponType())
                .couponStore(coupon.getCouponStore().getCouponStoreName())
                .build();
    }
}
