package com.ssak.ssak.domain.community;

import com.ssak.ssak.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글 ID로 모든 댓글 한 번에 조회 (작성자 정보까지 한 번에 조회)
    @Query("""
       select c from Comment c
       join fetch c.user
       left join fetch c.parent
       where c.post.postId = :postId
       order by c.parent.commentId asc nulls first, c.createdAt asc
    """)
    List<Comment> findAllByPostIdWithUser(@Param("postId") Long postId);

    List<Comment> findByUser(User user);
}
