package com.ssak.ssak.domain.community;

import com.ssak.ssak.domain.common.BaseEntity;
import com.ssak.ssak.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID")
    private Long commentId;

    @Column(name = "COMMENT_CONTENT")
    private String commentContent;

    @Column(name = "COMMENT_VISIBILITY")
    private Boolean commentVisibility = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_USER_COMMENT"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_POST_COMMENT"))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private Comment parent;                // 부모 댓글 (대댓글인 경우)

    //게시글 삭제 시 자식 모두 삭제하기 위해서 사용
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> children = new ArrayList<>();         // 자식 댓글들

    public void editComment(String commentContent) {
        this.commentContent = commentContent;
    }

    public void changeVisibility(boolean commentVisibility) {
        this.commentVisibility = commentVisibility;
    }
}
