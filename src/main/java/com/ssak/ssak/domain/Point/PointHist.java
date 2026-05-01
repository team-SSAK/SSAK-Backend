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
    private CouponHist couponHist=null;

    private PointHist(User user, CouponHist couponHist, int pointAmount, String pointDesc, PointType pointType) {
        this.user = user;
        this.couponHist = couponHist;
        this.pointAmount = pointAmount;
        this.pointDesc = pointDesc;
        this.pointType = pointType;
    }

    // 쿠폰 사용
    public static PointHist pointUseForCoupon(User user, CouponHist couponHist, int pointAmount) {
        String desc = couponHist.getCoupon().getCouponName() + " 교환";
        return new PointHist(user, couponHist, pointAmount, desc, PointType.USE);
    }

    // 포인트 적립
    public static PointHist savePoint(User user, int pointAmount, String pointDesc) {
        return new PointHist(user, null, pointAmount, pointDesc, PointType.SAVE);
    }
}
