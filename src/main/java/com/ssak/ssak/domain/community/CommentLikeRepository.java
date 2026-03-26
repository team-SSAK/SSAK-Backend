package com.ssak.ssak.domain.community;

import com.ssak.ssak.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUser_UserIdAndComment_CommentId(Long userId, Long commentId);

    void deleteAllByUser(User user);
}
