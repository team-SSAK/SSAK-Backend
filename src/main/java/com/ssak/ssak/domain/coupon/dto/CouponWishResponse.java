package com.ssak.ssak.domain.coupon.dto;

import com.ssak.ssak.domain.coupon.CouponWish;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponWishResponse {
    private Long couponWishId;
    private Long couponId;
    private String couponNm;
    private String couponStore;
    private int couponPoint;
    private String couponImgUrl;

    public static CouponWishResponse from(CouponWish couponWish) {
        return CouponWishResponse.builder()
                .couponWishId(couponWish.getCouponWishId())
                .couponId(couponWish.getCoupon().getCouponId())
                .couponNm(couponWish.getCoupon().getCouponName())
                .couponStore(couponWish.getCoupon().getCouponStore().getCouponStoreName())
                .couponPoint(couponWish.getCoupon().getCouponPoint())
                .couponImgUrl(couponWish.getCoupon().getCouponImgUrl())
                .build();
    }
}
