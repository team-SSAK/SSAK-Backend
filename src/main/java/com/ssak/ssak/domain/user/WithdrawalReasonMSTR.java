package com.ssak.ssak.domain.user;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "WD_REASON_MSTR")
public class WithdrawalReasonMSTR extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WD_REASON_MSTR_ID")
    private Long withdrawalReasonId;

    @Column(name = "WD_REASON_MSTR_CONTENT")
    private String withdrawalReasonContent;
}
