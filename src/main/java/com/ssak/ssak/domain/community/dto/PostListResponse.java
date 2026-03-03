package com.ssak.ssak.domain.community.dto;

import com.ssak.ssak.domain.community.Post;
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
    private String postContent;
    private boolean postVisibility;
    private String nickname;
    private LocalDateTime postCreateTime;
    private int postLikeCnt;
    private int postCommentCnt;

    public static PostListResponse from(Post post) {
        return PostListResponse.builder()
                .postId(post.getPostId())
                .postContent(post.getPostContent())
                .postVisibility(post.isPostVisibility())
                .nickname(post.getUser().getUserNm())
                .postCreateTime(post.getCreatedAt())
                .postLikeCnt(post.getPostLikeCnt())
                .postCommentCnt(post.getPostCommentCnt())
                .build();
    }
}
