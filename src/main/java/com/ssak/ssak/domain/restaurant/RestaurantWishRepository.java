package com.ssak.ssak.domain.restaurant;

import com.ssak.ssak.domain.restaurant.dto.RestaurantWishResponse;
import com.ssak.ssak.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantWishRepository extends JpaRepository<RestaurantWish, Long> {

    /**
     * 사용자의 식당 찜 목록을 조회한다.
     * @param userId
     * @return
     */
    List<RestaurantWish> findAllByUser_UserId(Long userId);

    /**
     * 특정 사용자의 특정 식당 찜 여부를 확인하고, 존재할 경우 찜 상세 내역을 반환한다.
     * @param userId
     * @param restaurantId
     * @return
     */
    Optional<RestaurantWish> findByUser_UserIdAndRestaurant_RestaurantId(Long userId, Long restaurantId);

    /**
     * 특정 사용자의 식당 찜목록을 삭제한다.
     * @param user
     */
    void deleteAllByUser(User user);
}
