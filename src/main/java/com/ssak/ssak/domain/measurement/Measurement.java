package com.ssak.ssak.domain.measurement;

import com.ssak.ssak.domain.common.BaseEntity;
import com.ssak.ssak.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Measurement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEASUREMENT_ID")
    private Long measurementId;

    @Column(name = "MM_PHOTO_URL")
    private String mmPhotoUrl;

    @Column(name = "LEFTOVER_RATIO")
    private Double leftoverRatio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_USER_MEASURED"))
    private User user;

    @Builder
    public Measurement(User user, String mmPhotoUrl, Double leftoverRatio) {
        this.user = user;
        this.mmPhotoUrl = mmPhotoUrl;
        this.leftoverRatio = leftoverRatio;
    }
}
