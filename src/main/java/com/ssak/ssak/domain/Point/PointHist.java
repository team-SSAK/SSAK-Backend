package com.ssak.ssak.domain.Point;

import com.ssak.ssak.domain.common.BaseEntity;
import com.ssak.ssak.domain.coupon.CouponHist;
import com.ssak.ssak.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POINT_HIST_ID")
    private Long pointHistId;

    @Column(name = "POINT_AMOUNT")
    private int pointAmount;

    @Column(name = "POINT_DESC", length = 200)
    private String pointDesc;

    @Enumerated(EnumType.STRING)
    @Column(name = "POINT_TYPE", nullable = false)
    private PointType pointType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_POINT_USER_ID"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUPON_HIST_ID", nullable = true, foreignKey = @ForeignKey(name = "FK_POINT_USED_COUPON"))
    private CouponHist couponHist;

    @Builder
    public PointHist(User user, CouponHist couponHist, int pointAmount) {
        this.user = user;
        this.couponHist = couponHist;
        this.pointAmount = pointAmount;
        this.pointDesc = couponHist.getCoupon().getCouponName() + " 교환";
        this.pointType = PointType.USE;
    }
}
