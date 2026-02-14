package com.ssak.ssak.domain.user;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification extends BaseEntity {
    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @MapsId // User의 PK를 그대로 PK로 사용 (1:1 공유 PK 방식)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "EVENT_NOTI_YN")
    private boolean eventNotiYn = true;     // 이벤트 및 혜택 알림

    @Column(name = "COMMUNITY_NOTI_YN")
    private boolean communityNotiYn = true;    // 커뮤니티 알림

    @Column(name = "NIGHT_NOTI_YN")
    private boolean nightNotiYn = false;    // 야간 알림

    @Builder
    public Notification(User user, Boolean eventNotiYn, Boolean communityNotiYn, Boolean nightNotiYn) {
        this.user = user;
        this.eventNotiYn = (eventNotiYn != null) ? eventNotiYn : false;
        this.communityNotiYn = (communityNotiYn != null) ? communityNotiYn : true;
        this.nightNotiYn = (nightNotiYn != null) ? nightNotiYn : false;
    }

    public void updateNotification(Boolean eventNotiYn, Boolean communityNotiYn, Boolean nightNotiYn) {
        if(eventNotiYn != null) this.eventNotiYn  = eventNotiYn;
        if(communityNotiYn != null) this .communityNotiYn  = communityNotiYn;
        if(nightNotiYn != null) this.nightNotiYn  = nightNotiYn;
    }
}
