package com.ssak.ssak.domain.measurement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    int countByUserUserIdAndCreatedAtAfter(Long userId, LocalDateTime startOfDay);

    Optional<Measurement> findFirstByUserUserIdOrderByCreatedAtDesc(Long userId);
}
