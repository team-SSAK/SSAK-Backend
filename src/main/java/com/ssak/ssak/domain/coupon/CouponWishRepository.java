package com.ssak.ssak.domain.coupon;

import com.ssak.ssak.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponWishRepository extends JpaRepository<CouponWish, Long> {

    /**
     * 특정 사용자의 쿠폰 찜 목록을 조회한다.
     * @param userId
     * @return
     */
    @Query("SELECT cw FROM CouponWish cw " +
           "JOIN FETCH cw.coupon " +
           "JOIN FETCH cw.user " +  //user도 같이 가져오도록 수정
           "WHERE cw.user.userId = :userId")
    List<CouponWish> findAllByUser_UserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 특정 쿠폰 찜 여부를 확인하고, 존재할 경우 찜 상세 내역을 반환한다.
     * @param userId
     * @param couponId
     * @return
     */
    Optional<CouponWish> findByUser_UserIdAndCoupon_CouponId(Long userId, Long couponId);

    /**
     * 특정 사용자의 쿠폰 찜 목록을 삭제한다.
     * @param user
     */
    void deleteAllByUser(User user);
}
