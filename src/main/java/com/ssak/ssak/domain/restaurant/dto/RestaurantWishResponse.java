package com.ssak.ssak.domain.restaurant.dto;

import com.ssak.ssak.domain.restaurant.RestaurantWish;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantWishResponse {
    private Long restaurantWishId;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantLocation;
    private String restaurantImgUrl;

    public static RestaurantWishResponse from(RestaurantWish restaurantWish) {
        return RestaurantWishResponse.builder()
                .restaurantWishId(restaurantWish.getRestaurantWishId())
                .restaurantId(restaurantWish.getRestaurant().getRestaurantId())
                .restaurantName(restaurantWish.getRestaurant().getRestaurantName())
                .restaurantLocation(restaurantWish.getRestaurant().getRestaurantLocation())
                .restaurantImgUrl(restaurantWish.getRestaurant().getRestaurantImgUrl())
                .build();
    }
}
