package com.ssak.ssak.domain.community;

import com.ssak.ssak.domain.common.BaseEntity;
import com.ssak.ssak.domain.restaurant.Restaurant;
import com.ssak.ssak.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_ID")
    private Long postId;

    @Column(name = "POST_TITLE")
    private String postTitle;

    @Column(name = "POST_CONTENT")
    private String postContent;

    @Column(name = "POST_VISIBILITY")
    private Boolean postVisibility = true;

    @Column(name = "POST_LIKE_CNT")
    private int postLikeCnt = 0;

    @Column(name = "POST_COMMENT_CNT")
    private int postCommentCnt = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_POST_WRITER"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REST_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_POST_RESTAURANT"))
    private Restaurant restaurant;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostPhoto> postPhotos;

    @OneToMany(mappedBy = "post",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postWishes;

    public void addComment() {
        postCommentCnt++;
    }

    public void deleteComment() {
        postCommentCnt--;
    }

    public void addLiked() {
        postLikeCnt++;
    }

    public void deleteLiked() {
        postLikeCnt--;
    }

    public void editPost(String postTitle, String postContent, Boolean postVisibility) {
        if(postTitle != null) this.postTitle = postTitle;
        if(postContent != null) this.postContent = postContent;
        if(postVisibility != null) this.postVisibility = postVisibility;
    }
}
