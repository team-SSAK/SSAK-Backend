package com.ssak.ssak.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일 중복 여부 검사
    boolean existsByUserEmail(String userEmail);

    // 이메일로 사용자 조회
    Optional<User> findByUserEmail(String userEmail);

    Optional<User> findByUserId(Long userId);
}
