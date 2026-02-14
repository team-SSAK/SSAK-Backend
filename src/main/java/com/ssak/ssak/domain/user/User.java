package com.ssak.ssak.domain.user;

import com.ssak.ssak.domain.common.BaseEntity;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(unique = true, name = "USER_EMAIL")
    private String userEmail;           // 사용자 이메일 (로그인 ID)

    @Column(name = "USER_PW")
    private String userPw;        // 비밀번호

    @Column(name = "USER_NM")
    private String userNm;        // 닉네임

    @Enumerated(EnumType.STRING)
    private LoginType loginType;    // 로그인 방식

    @Column(length = 50)
    private String providerId;      // 소셜 로그인 고유 ID

    @Column(name = "USER_POINT", nullable = false)
    private int currentPoint = 0;   // 사용자의 현재 포인트

    public void changePassword(String newPassword) {
        this.userPw = newPassword;
    }

    @Builder
    public User(String userEmail, String userPw, String userNm, LoginType loginType, String providerId) {
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userNm = userNm;
        this.loginType = loginType;
        this.providerId = providerId;
    }

    // 프로필 수정
    public void modifyProfile(String userNm) {
        if(userNm != null) this.userNm = userNm;
    }

    // 포인트 추가
    public void addPoint(int point) {
        this.currentPoint = point;
    }
    // 포인트 차감
    public void removePoint(int point) {
        if(this.currentPoint < point) {
            throw new CustomException(ErrorCode.INSUFFICIENT_POINTS);
        }
    }
}