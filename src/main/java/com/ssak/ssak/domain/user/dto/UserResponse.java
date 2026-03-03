package com.ssak.ssak.domain.user.dto;

import com.ssak.ssak.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userEmail;
    private String userNm;
    private int userPoint;
    private String userProfileImg;

    public static UserResponse from(User user) {
        return new UserResponse(
          user.getUserEmail(),
          user.getUserNm(),
          user.getCurrentPoint(),
          user.getUserProfileImg()
        );
    }
}