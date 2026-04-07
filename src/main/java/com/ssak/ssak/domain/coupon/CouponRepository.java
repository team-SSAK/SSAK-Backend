package com.ssak.ssak.domain.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findAllByCouponValidTrue();

    List<Coupon> findAllByCouponValidTrueAndCouponType(CouponType type);
}
