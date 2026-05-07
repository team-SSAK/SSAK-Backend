package com.ssak.ssak.service;

import com.ssak.ssak.domain.restaurant.*;
import com.ssak.ssak.domain.restaurant.dto.*;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantWishRepository restaurantWishRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;

    /**
     * 사용자가 찜한 식당 목록을 조회한다
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public List<RestaurantListResponse> getRestaurantList(Long userId) {
        List<Restaurant> restaurants = restaurantRepository.findAll();

        // 2. 사용자가 찜한 식당 ID들만 조회
        Set<Long> wishedRestaurantIds = restaurantWishRepository.findAllByUser_UserId(userId)
                .stream()
                .map(wish -> wish.getRestaurant().getRestaurantId())
                .collect(Collectors.toSet());

        // 3. 메모리에서 매핑
        return restaurants.stream()
                .map(r -> RestaurantListResponse.from(r, wishedRestaurantIds.contains(r.getRestaurantId())))
                .collect(Collectors.toList());
    }

    /**
     * 특정 식당 정보를 조회한다.
     * @param restId
     * @return
     */
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantDetail(Long restId) {
        Restaurant restaurant = restaurantRepository.findById(restId).orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));

        return RestaurantResponse.from(restaurant);
    }


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

    /**
     * 해당 식당의 오늘의 메뉴를 조회합니다.
     * @param restId
     * @return
     */
    @Transactional(readOnly = true)
    public List<MenuResponse> getTodayMenu(Long restId) {
        Restaurant restaurant = restaurantRepository.findById(restId).orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));

        // 1. 오늘의 날짜를 계산
        LocalDate today = LocalDate.now();

        // 2. 오늘에 해당하는 메뉴 찾기
        List<Menu> todayMenu = menuRepository.findAllByRestaurantAndMenuDate(restaurant, today);

        // 3. 반환  // TODO - N+1문제 없는지 확인필요
        return todayMenu.stream()
                .map(menu -> MenuResponse.builder()
                        .menuId(menu.getMenuId())
                        .menuType(menu.getMenuType())
                        .menuItems(menu.getMenuItems().stream().map(MenuItem::getMenuItemNm)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
