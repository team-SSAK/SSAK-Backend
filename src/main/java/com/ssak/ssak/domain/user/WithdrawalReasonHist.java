package com.ssak.ssak.domain.user;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "WD_REASON_HIST")
public class WithdrawalReasonHist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WD_REASON_HIST_ID")
    private Long withdrawalReasonResponseId;

    @Column(name = "WD_REASON_DETAIL", nullable = true)
    private String withdrawalReasonDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "WD_REASON_MSTR_ID", nullable = false)
    private WithdrawalReasonMSTR withdrawalReasonMSTR;

    @Builder
    public WithdrawalReasonHist(WithdrawalReasonMSTR withdrawalReasonMSTR, String withdrawalReasonDetail) {
        this.withdrawalReasonMSTR = withdrawalReasonMSTR;
        this.withdrawalReasonDetail = withdrawalReasonDetail;
    }
}