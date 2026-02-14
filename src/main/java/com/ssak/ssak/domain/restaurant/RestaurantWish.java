package com.ssak.ssak.domain.restaurant;

import com.ssak.ssak.domain.common.BaseEntity;
import com.ssak.ssak.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RestaurantWish extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REST_WISH_ID")
    private Long restaurantWishId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_REST_WISHER"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REST_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_WISHED_REST"))
    private Restaurant restaurant;

    public static RestaurantWish createRestaurantWish(User user, Restaurant restaurant) {
        return RestaurantWish.builder()
                .user(user)
                .restaurant(restaurant)
                .build();
    }
}
