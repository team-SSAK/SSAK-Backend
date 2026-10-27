package com.ssak.ssak.domain.measurement;

import com.ssak.ssak.domain.common.BaseEntity;
import com.ssak.ssak.domain.restaurant.Restaurant;
import com.ssak.ssak.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Measurement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEASUREMENT_ID")
    private Long measurementId;

    @Column(name = "MM_PHOTO_URL")
    private String mmPhotoUrl;

    @Column(name = "LEFTOVER_RATIO")
    private Double leftoverRatio;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "USER_ID", nullable = true, foreignKey = @ForeignKey(name = "FK_USER_MEASURED"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "RESTAURANT_ID", nullable = true)
    private Restaurant restaurant;

    @Column(name = "SHOT_LAT", nullable = true)
    private Double shotLat;

    @Column(name = "SHOT_LON", nullable = true)
    private Double shotLon;

    @Builder
    public Measurement(User user, String mmPhotoUrl, Double leftoverRatio, Restaurant restaurant, Double shotLat, Double shotLon) {
        this.user = user;
        this.mmPhotoUrl = mmPhotoUrl;
        this.leftoverRatio = leftoverRatio;
        this.restaurant = restaurant;
        this.shotLat = shotLat;
        this.shotLon = shotLon;
    }

    public void removeUser() {
        this.user = null;
    }
}
