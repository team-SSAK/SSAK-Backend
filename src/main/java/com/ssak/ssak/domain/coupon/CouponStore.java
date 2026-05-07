package com.ssak.ssak.domain.coupon;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponStore extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@Column(name = "COUPON_STORE_ID")
    private Long id;

    @Column(name = "COUPON_STORE_NM")
    private String couponStoreName;

    @Column(name = "COUPON_STORE_PW")
    private int couponStorePassword;
}
