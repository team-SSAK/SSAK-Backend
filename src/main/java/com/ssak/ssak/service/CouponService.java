package com.ssak.ssak.service;

import com.ssak.ssak.domain.Point.PointHist;
import com.ssak.ssak.domain.Point.PointHistRepository;
import com.ssak.ssak.domain.coupon.*;
import com.ssak.ssak.domain.coupon.dto.*;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final CouponHistRepository couponHistRepository;
    private final CouponWishRepository couponWishRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final PointHistRepository pointHistRepository;

    /**
     * 특정 사용자의 특정 상태의 쿠폰 목록을 조회한다.
     * @param userId
     * @param option
     * @return
     */
    @Transactional(readOnly = true)
    public List<CouponHistResponse> getMyCouponList(Long userId, CouponStatus option) {
        // 1. 해당 유저의 쿠폰 발급 내역 조회
        List<CouponHist> coupons = couponHistRepository.findAllByUser_UserIdAndCouponStatus(userId, option);

        // 2. 해당 유저가 찜한 쿠폰 목록 조회 (set사용)
        Set<Long> wishedCouponIds = couponWishRepository.findAllByUser_UserId(userId)
                .stream()
                .map(wish -> wish.getCoupon().getCouponId())
                .collect(Collectors.toSet());

        // 3. 발급된 각 쿠폰에 대하여 찜 여부 판단
        return coupons.stream()
                .map(couponHist -> {
                    Long couponId = couponHist.getCoupon().getCouponId();
                    boolean isWished = wishedCouponIds.contains(couponId);
                    return CouponHistResponse.from(couponHist, isWished);
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 쿠폰 찜 목록을 조회한다.
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public List<CouponWishResponse> getMyWishCouponList(Long userId) {
        List<CouponWish> coupons = couponWishRepository.findAllByUser_UserId(userId);

        return coupons.stream()
                .map(CouponWishResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 특정 쿠폰 찜을 등록 / 취소 한다.
     * @param userId
     * @param couponId
     * @return
     */
    @Transactional
    public CouponWishActionResponse updateCouponWishStatus(Long userId, Long couponId) {
        // 1. 기존 찜 내역이 있는 지 조회
        Optional<CouponWish> couponWish = couponWishRepository.findByUser_UserIdAndCoupon_CouponId(userId, couponId);

        // 2. 찜 내역이 존재한다면, 찜 취소
        if (couponWish.isPresent()) {
            couponWishRepository.delete(couponWish.get());
            return new CouponWishActionResponse(false, couponId);
        } else { // 3. 찜 내역이 존재하지 않는다면, 찜하기
            User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Coupon coupon = couponRepository.findById(couponId).orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));
            couponWishRepository.save(CouponWish.createCouponWish(user, coupon));
            return new CouponWishActionResponse(true, couponId);
        }
    }

    /**
     * 쿠폰 목록을 조회한다.
     * @param type
     * @return
     */
    @Transactional(readOnly = true)
    public List<CouponListResponse> getCouponList(CouponType type, Long userId) {
        // 유효한 쿠폰 전체 조회
        List<Coupon> coupons;
        if(type == null) {
            coupons = couponRepository.findAllByCouponValidTrue();
        } else { // 타입별 조회
            coupons = couponRepository.findAllByCouponValidTrueAndCouponType(type);
        }

        // 찜 목록 가져오기
        Set<Long> wishedCouponIds = new HashSet<>();
        wishedCouponIds = couponWishRepository.findAllByUser_UserId(userId).stream()
                .map(wish -> wish.getCoupon().getCouponId())
                .collect(Collectors.toSet());

        Set<Long> finalWishedCouponIds = wishedCouponIds;
        return coupons.stream()
                .map(coupon -> {
                    boolean isWished = finalWishedCouponIds.contains(coupon.getCouponId());
                    return CouponListResponse.from(coupon, isWished);
                })
                .collect(Collectors.toList());
    }

    /**
     * 쿠폰 상세정보를 조회한다.
     * @param couponId
     * @return
     */
    @Transactional(readOnly = true)
    public CouponResponse getCouponDetail(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));
        return CouponResponse.from(coupon);
    }

    /**
     * 포인트를 쿠폰으로 교환한다.
     * @param userId
     * @param exchangeCouponId
     * @return
     */
    @Transactional
    public CouponExchangeResponse exchangeIntoCoupon(Long userId, Long exchangeCouponId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Coupon coupon = couponRepository.findById(exchangeCouponId).orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

        // 1. 보유 포인트가 충분한 지 확인
        if (user.getCurrentPoint() < coupon.getCouponPoint()) {
            throw new CustomException(ErrorCode.INSUFFICIENT_POINTS);
        }

        // 2. 사용자 포인트 차감
        user.removePoint(coupon.getCouponPoint());

        // 3. 사용자에게 쿠폰 발급
        CouponHist couponHist = CouponHist.builder()
                .coupon(coupon)
                .user(user)
                .build();
        couponHistRepository.save(couponHist);

        // 4. 포인트 사용이력에 추가
        PointHist pointHist = PointHist.pointUseForCoupon(
                user,
                couponHist,
                coupon.getCouponPoint()
        );
        pointHistRepository.save(pointHist);

        return CouponExchangeResponse.from(coupon, user, couponHist);
    }

    @Transactional
    public CouponUseResponse useCoupon(Long userId, CouponUseRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        CouponHist couponHist = couponHistRepository.findById(request.getCouponHistId()).orElseThrow(() -> new CustomException(ErrorCode.INVALID_COUPON));

        // 쿠폰 사용 인증 번호 확인
        int storePw = couponHist.getCoupon().getCouponStore().getCouponStorePassword();
        if (request.getStorePw() != storePw) {
            throw new CustomException(ErrorCode.INVALID_STORE_PASSWORD);
        }

        // 쿠폰 사용 처리
        couponHist.changeCouponStatus(CouponStatus.USED);
        return CouponUseResponse.from(couponHist);
    }
}
