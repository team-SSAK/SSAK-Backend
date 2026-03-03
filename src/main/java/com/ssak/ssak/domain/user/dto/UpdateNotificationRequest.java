package com.ssak.ssak.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationRequest {
    private boolean eventNotiYn;
    private boolean communityNotiYn;
    private boolean nightNotiYn;
}
