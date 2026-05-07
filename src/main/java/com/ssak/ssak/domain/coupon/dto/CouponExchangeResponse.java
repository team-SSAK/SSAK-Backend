package com.ssak.ssak.domain.coupon.dto;

import com.ssak.ssak.domain.coupon.Coupon;
import com.ssak.ssak.domain.coupon.CouponHist;
import com.ssak.ssak.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponExchangeResponse {
    Long couponId;
    String couponName;
    int spentPoint;
    int remainingPoint;
    LocalDateTime expirationDate;

    public static CouponExchangeResponse from (Coupon coupon, User user, CouponHist couponHist) {
        return CouponExchangeResponse.builder()
                .couponId(couponHist.getCouponHistId())
                .couponName(coupon.getCouponName())
                .spentPoint(coupon.getCouponPoint())
                .remainingPoint(user.getCurrentPoint())
                .expirationDate(coupon.getCreatedAt().plusDays(180))  //일단 발급일로부터 180일(6개월)로 해둠
                .build();
    }
}
