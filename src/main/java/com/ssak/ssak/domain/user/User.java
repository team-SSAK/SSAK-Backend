package com.ssak.ssak.domain.user;

import com.ssak.ssak.domain.common.entity.BaseEntity;
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
    private Long memberId;

    @Column(unique = true, name = "USER_EMAIL")
    private String email;           // 사용자 이메일 (로그인 ID)

    @Column(name = "USER_PW")
    private String password;        // 비밀번호

    @Column(name = "USER_NM")
    private String nickname;        // 닉네임

    @Column(name = "AGREE_YN")
    private boolean agree;          // 마케팅 수신 여부 (선택 동의 항목)

    @Enumerated(EnumType.STRING)
    private LoginType loginType;    // 로그인 방식

    @Column(length = 50)
    private String providerId;      // 소셜 로그인 고유 ID

    @Builder
    public User(String email, String password, String nickname, Boolean isAgree, LoginType loginType, String providerId) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.agree = isAgree;
        this.loginType = loginType;
        this.providerId = providerId;
    }
}