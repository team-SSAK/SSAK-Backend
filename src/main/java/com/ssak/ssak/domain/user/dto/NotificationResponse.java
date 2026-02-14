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
    private boolean communityYn;
    private boolean nightYn;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .eventNotiYn(notification.isEventNotiYn())
                .communityYn(notification.isCommunityNotiYn())
                .nightYn(notification.isNightNotiYn())
                .build();
    }
}
