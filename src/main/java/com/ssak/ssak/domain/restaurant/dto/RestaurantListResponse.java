package com.ssak.ssak.domain.restaurant.dto;

import com.ssak.ssak.domain.restaurant.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantListResponse {
    private Long restaurantId;
    private String restaurantName;
    private String restaurantLocation;
    private String restaurantImgUrl;
    private boolean isWished;
    private Double latitude;
    private Double longitude;

    public static RestaurantListResponse from(Restaurant restaurant, boolean isWished) {
        Double latitude = null;
        Double longitude = null;

        if (restaurant.getRestaurantCoord() != null) {
            latitude = restaurant.getRestaurantCoord().getY();
            longitude = restaurant.getRestaurantCoord().getX();
        }

        return RestaurantListResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .restaurantName(restaurant.getRestaurantName())
                .restaurantLocation(restaurant.getRestaurantLocation())
                .restaurantImgUrl(restaurant.getRestaurantImgUrl())
                .isWished(isWished)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
