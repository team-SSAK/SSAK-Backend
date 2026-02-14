package com.ssak.ssak.domain.coupon;

import com.ssak.ssak.domain.common.BaseEntity;
import com.ssak.ssak.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponHist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPON_HIST_ID")
    private Long couponHistId;

    @Enumerated(EnumType.STRING)
    @Column(name = "COUPON_ST", nullable = false)
    private CouponStatus couponStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_COUPON_USER"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUPON_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_USED_COUPON"))
    private Coupon coupon;
}
