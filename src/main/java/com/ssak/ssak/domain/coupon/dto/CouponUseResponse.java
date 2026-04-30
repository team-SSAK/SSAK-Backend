package com.ssak.ssak.domain.coupon.dto;

import com.ssak.ssak.domain.coupon.CouponHist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUseResponse {
    private Long couponHistId;
    private String couponName;
    private LocalDateTime usedAt;

    public static CouponUseResponse from(CouponHist couponHist) {
        return CouponUseResponse.builder()
                .couponHistId(couponHist.getCouponHistId())
                .couponName(couponHist.getCoupon().getCouponName())
                .usedAt(couponHist.getUpdatedAt())
                .build();
    }
}
