package com.ssak.ssak.domain.Point.dto;

import com.ssak.ssak.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointCurrentResponse {
    private int currentPoint;

    public static PointCurrentResponse from(User user) {
        return PointCurrentResponse.builder().currentPoint(user.getCurrentPoint()).build();
    }
}
