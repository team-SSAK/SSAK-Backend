package com.ssak.ssak.domain.user.dto;

import com.ssak.ssak.domain.user.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {
    private boolean eventNotiYn;
    private boolean communityNotiYn;
    private boolean nightNotiYn;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .eventNotiYn(notification.isEventNotiYn())
                .communityNotiYn(notification.isCommunityNotiYn())
                .nightNotiYn(notification.isNightNotiYn())
                .build();
    }
}
