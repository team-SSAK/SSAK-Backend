package com.ssak.ssak.domain.notice.dto;

import com.ssak.ssak.domain.notice.Notice;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class NoticeListResponse {
    private Long noticeId;
    private String noticeTitle;
    private LocalDateTime noticeCreateTime;

    public static NoticeListResponse from(Notice notice) {
        return NoticeListResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeCreateTime(notice.getUpdatedAt())
                .build();
    }
}
