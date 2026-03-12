package com.ssak.ssak.domain.community.dto;

import com.ssak.ssak.domain.community.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private Long postId;
    private boolean postVisibility;
    private String postTitle;
    private String postContent;
    private LocalDateTime postCreateTime;
    private String nickname;
    private int postLikeCnt;
    private int postCommentCnt;

    private List<String> imageUrls;
    private List<CommentResponse> comments;

    public static PostResponse from(Post post, List<String> imageUrls, List<CommentResponse> comments) {
        return PostResponse.builder()
                .postId(post.getPostId())
                .postVisibility(post.isPostVisibility())
                .postTitle(post.getPostTitle())
                .postContent(post.getPostContent())
                .postCreateTime(post.getCreatedAt())
                .nickname(post.getUser().getUserNm())
                .postLikeCnt(post.getPostLikeCnt())
                .postCommentCnt(post.getPostCommentCnt())
                .imageUrls(imageUrls)
                .comments(comments)
                .build();
    }
}
