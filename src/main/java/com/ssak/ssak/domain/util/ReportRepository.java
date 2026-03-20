package com.ssak.ssak.domain.util;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    int countByTargetIdAndTargetType(Long postId, ReportType reportType);

    boolean existsByReporterIdAndTargetIdAndTargetType(Long userId, Long postId, ReportType reportType);
}
