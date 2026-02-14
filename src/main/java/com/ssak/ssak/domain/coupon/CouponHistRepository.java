package com.ssak.ssak.domain.coupon;

import com.ssak.ssak.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponHistRepository extends JpaRepository<CouponHist, Long> {

    /**
     * 특정 유저의 특정 상태(사용가능/사용완료 등) 쿠폰 목록을 조회
     * @param userId
     * @param option
     * @return
     */
    List<CouponHist> findAllByUser_UserIdAndCouponStatus(Long userId, CouponStatus option);

    /**
     * 특정 유저에 해당하는 데이터 모두 삭제
     * @param user
     */
    void deleteAllByUser(User user);
}
