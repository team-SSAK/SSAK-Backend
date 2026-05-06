package com.ssak.ssak.domain.restaurant.dto;

import com.ssak.ssak.domain.restaurant.Restaurant;
import com.ssak.ssak.domain.restaurant.RestaurantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.locationtech.jts.geom.Point;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponse {
    private Long restaurantId;
    private String restaurantName;
    private String restaurantLocation;
    private RestaurantType restaurantType;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String restaurantImgUrl;
    private Double latitude;
    private Double longitude;

    public static RestaurantResponse from(Restaurant restaurant) {
        Double latitude = null;
        Double longitude = null;

        if (restaurant.getRestaurantCoord() != null) {
            // Point 객체의 위도와 경도값 추출
            latitude = restaurant.getRestaurantCoord().getY();
            longitude = restaurant.getRestaurantCoord().getX();
        }
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .restaurantName(restaurant.getRestaurantName())
                .restaurantLocation(restaurant.getRestaurantLocation())
                .restaurantType(restaurant.getRestaurantType())
                .openTime(restaurant.getOpenTime())
                .closeTime(restaurant.getCloseTime())
                .latitude(latitude)
                .longitude(longitude)
                .restaurantImgUrl(restaurant.getRestaurantImgUrl())
                .build();
    }
}
