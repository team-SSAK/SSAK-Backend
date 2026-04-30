package com.ssak.ssak.domain.coupon;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPON_ID")
    private Long couponId;

    @Column(name = "COUPON_NM", nullable = false)
    private String couponName;

    @Column(name = "COUPON_DESC")
    private String couponDesc;     // 쿠폰 설명 (이미지)

    @Column(name = "COUPON_POINT", nullable = false)
    private int couponPoint;    // 쿠폰 포인트

    @Enumerated(EnumType.STRING)
    @Column(name = "COUPON_TYPE")
    private CouponType couponType=CouponType.ETC;  // 쿠폰 타입

    @Column(name = "COUPON_IMG")
    private String couponImgUrl; // 쿠폰 대표 이미지

    @Column(name = "COUPON_VALID_TERM")
    private int couponValidTerm;  // 쿠폰 기간

    @Column(name = "COUPON_VALID", nullable = false)
    private boolean couponValid = true;   // 쿠폰 유효 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUPON_STORE_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_COUPON_COUPONSTORE"))
    private CouponStore couponStore;
}
