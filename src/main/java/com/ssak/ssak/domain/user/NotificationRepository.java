package com.ssak.ssak.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 사용자의 알림을 지운다.
     * @param user
     */
    void deleteAllByUser(User user);
}
