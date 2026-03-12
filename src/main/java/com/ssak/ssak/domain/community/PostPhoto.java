package com.ssak.ssak.domain.community;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostPhoto extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_PHOTO_ID")
    private Long postPhotoId;

    @Column(name = "POST_PHOTO_URL")
    private String postPhotoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_PHOTO_POST"))
    private Post post;
}
