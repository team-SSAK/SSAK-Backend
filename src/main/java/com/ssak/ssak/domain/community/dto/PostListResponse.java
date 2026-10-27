package com.ssak.ssak.domain.community.dto;

import com.ssak.ssak.domain.community.Post;
import com.ssak.ssak.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListResponse {
    private Long postId;
    private String postTitle;
    private String postContent;
    private Boolean postVisibility;
    private String nickname;
    private LocalDateTime postCreateTime;
    private int postLikeCnt;
    private int postCommentCnt;
    private boolean ownerPost;

    public static PostListResponse from(Post post) {
        return from(post, false);
    }

    public static PostListResponse from(Post post, boolean ownerPost) {
        String userNm = null;

        if (post.getUser() != null) {
            userNm = post.getUser().getUserNm();
        }
        return PostListResponse.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postContent(post.getPostContent())
                .postVisibility(post.getPostVisibility())
                .nickname(userNm)
                .postCreateTime(post.getCreatedAt())
                .postLikeCnt(post.getPostLikeCnt())
                .postCommentCnt(post.getPostCommentCnt())
                .ownerPost(ownerPost)
                .build();
    }
}
