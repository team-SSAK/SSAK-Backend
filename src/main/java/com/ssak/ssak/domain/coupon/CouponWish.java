package com.ssak.ssak.domain.coupon;

import com.ssak.ssak.domain.common.BaseEntity;
import com.ssak.ssak.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponWish extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPON_WISH_ID")
    private Long couponWishId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_COUPON_WISHER"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUPON_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_COUPON_WISHED"))
    private Coupon coupon;

    public static CouponWish createCouponWish(User user, Coupon coupon) {
        return CouponWish.builder()
                .user(user)
                .coupon(coupon)
                .build();
    }
}
