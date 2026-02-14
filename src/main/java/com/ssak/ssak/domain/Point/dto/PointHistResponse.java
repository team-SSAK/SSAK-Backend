package com.ssak.ssak.domain.Point.dto;

import com.ssak.ssak.domain.Point.PointHist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistResponse {
    private Long pointHistId;
    private int pointAmount;
    private String pointDesc;
    private PointType pointType;
    private LocalDateTime pointTime;

    public static PointHistResponse from(PointHist pointHist) {
        return PointHistResponse.builder()
                .pointHistId(pointHist.getPointHistId())
                .pointAmount(pointHist.getPointAmount())
                .pointDesc(pointHist.getPointDesc())
                .pointType(pointHist.getPointType())
                .pointTime(pointHist.getCreatedAt())
                .build();
    }
}
