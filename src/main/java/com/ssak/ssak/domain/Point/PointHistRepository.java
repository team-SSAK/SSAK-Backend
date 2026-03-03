package com.ssak.ssak.domain.Point;

import com.ssak.ssak.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistRepository extends JpaRepository<PointHist, Long> {

    /**
     * 특정 사용자의 포인트 내역을 최신순으로 모두 조회한다.
     * @param userId
     * @return
     */
    List<PointHist> findAllByUser_UserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 사용자의 특정 상태에 해당하는 포인트 내역을 모두 조회한다.
     * @param userId
     * @param pointType
     * @return
     */
    List<PointHist> findAllByUser_UserIdAndPointTypeOrderByCreatedAtDesc(Long userId, PointType pointType);

    /**
     * 특정 사용자에 해당되는 데이터를 삭제한다.
     * @param user
     */
    void deleteAllByUser(User user);
}
