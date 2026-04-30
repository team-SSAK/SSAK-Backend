package com.ssak.ssak.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponUseRequest {
    private Long couponHistId;
    private int storePw;
}
