package com.ssak.ssak.service;

import com.ssak.ssak.domain.restaurant.Restaurant;
import com.ssak.ssak.domain.restaurant.RestaurantRepository;
import com.ssak.ssak.domain.restaurant.RestaurantWish;
import com.ssak.ssak.domain.restaurant.RestaurantWishRepository;
import com.ssak.ssak.domain.restaurant.dto.RestaurantWishActionResponse;
import com.ssak.ssak.domain.restaurant.dto.RestaurantWishResponse;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantWishRepository restaurantWishRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * 로그인한 사용자가 찜한 식당 목록 조회
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public List<RestaurantWishResponse> getMyWishRestaurantList(Long userId) {
        List<RestaurantWish> restaurants = restaurantWishRepository.findAllByUser_UserId(userId);

        return restaurants.stream()
                .map(RestaurantWishResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 특정 식당 찜을 등록 / 취소 한다.
     * @param userId
     * @param restaurantId
     * @return
     */
    @Transactional
    public RestaurantWishActionResponse updateRestaurantWishStatus(Long userId, Long restaurantId) {
        // 1. 기존 찜 내역이 있는지 조회
        Optional<RestaurantWish> restaurantWish = restaurantWishRepository.findByUser_UserIdAndRestaurant_RestaurantId(userId, restaurantId);
        // 2. 찜 내역이 존재한다면, 찜 취소
        if (restaurantWish.isPresent()) {
            restaurantWishRepository.delete(restaurantWish.get());
            return new RestaurantWishActionResponse(false, restaurantId);
        } else {
            // 3. 찜 내역이 존재하지 않는다면, 찜하기
            User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));
            restaurantWishRepository.save(RestaurantWish.createRestaurantWish(user, restaurant));
            return new RestaurantWishActionResponse(true, restaurantId);
        }
    }
}
