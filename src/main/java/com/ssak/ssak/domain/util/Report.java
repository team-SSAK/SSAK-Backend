package com.ssak.ssak.domain.util;

import com.ssak.ssak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPORT_ID")
    private Long reportId;

    @Column(name = "REPORTER")
    private Long reporterId;

    @Column(name = "TARGET_ID")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_TYPE")
    private ReportType targetType;

    @Column(name = "REPORT_CONTENT")
    private String reportContent;
}
