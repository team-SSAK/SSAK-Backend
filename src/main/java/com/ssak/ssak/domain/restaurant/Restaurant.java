package com.ssak.ssak.domain.restaurant;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "REST_TYPE")
    private RestaurantType restaurantType=RestaurantType.UNKNOWN;

    @Column(name = "REST_COORD", columnDefinition = "POINT SRID 4326")
    private Point restaurantCoord;

}
