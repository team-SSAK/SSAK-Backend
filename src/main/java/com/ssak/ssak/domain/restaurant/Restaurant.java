package com.ssak.ssak.domain.restaurant;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REST_ID")
    private Long restaurantId;

    @Column(name = "REST_NM", nullable = false)
    private String restaurantName;

    @Column(name = "REST_LOCATION")
    private String restaurantLocation;

    @Column(name = "REST_OPEN_TIME")
    private LocalTime openTime;

    @Column(name = "REST_CLOSE_TIME")
    private LocalTime closeTime;

    @Column(name = "REST_IMG")
    private String restaurantImgUrl;

    @Column(name = "REST_TYPE")
    private RestaurantType restaurantType;
}
