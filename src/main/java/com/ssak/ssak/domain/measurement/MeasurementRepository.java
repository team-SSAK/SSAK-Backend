package com.ssak.ssak.domain.measurement;

import com.ssak.ssak.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    int countByUserUserIdAndCreatedAtAfter(Long userId, LocalDateTime startOfDay);

    Optional<Measurement> findFirstByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Measurement> findByUser(User user);
}
