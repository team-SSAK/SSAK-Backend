package com.ssak.ssak.domain.user;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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

    @Column(name = "MARKETING_AGREE_YN")
    private boolean marketingAgreeYn;          // 마케팅 수신 여부 (선택 동의 항목)

    @Enumerated(EnumType.STRING)
    private LoginType loginType;    // 로그인 방식

    @Column(length = 50)
    private String providerId;      // 소셜 로그인 고유 ID

    public void changePassword(String newPassword) {
        this.userPw = newPassword;
    }

    @Builder
    public User(String userEmail, String userPw, String userNm, Boolean marketingAgreeYn, LoginType loginType, String providerId) {
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userNm = userNm;
        this.marketingAgreeYn = marketingAgreeYn;
        this.loginType = loginType;
        this.providerId = providerId;
    }
}