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
    private Boolean postVisibility;
    private String postTitle;
    private String postContent;
    private LocalDateTime postCreateTime;
    private String nickname;
    private int postLikeCnt;
    private int postCommentCnt;

    private List<String> imageUrls;
    private List<CommentResponse> comments;

    public static PostResponse from(Post post, List<String> imageUrls, List<CommentResponse> comments) {
        // 1. 닉네임 기본값 설정
        String nickname = "탈퇴한 사용자";

        // 2. post.getUser()가 null이 아닌 경우에만 닉네임 가져오기
        if (post.getUser() != null) {
            nickname = post.getUser().getUserNm();
        }

        return PostResponse.builder()
                .postId(post.getPostId())
                .postVisibility(post.getPostVisibility())
                .postTitle(post.getPostTitle())
                .postContent(post.getPostContent())
                .postCreateTime(post.getCreatedAt())
                .nickname(nickname)
                .postLikeCnt(post.getPostLikeCnt())
                .postCommentCnt(post.getPostCommentCnt())
                .imageUrls(imageUrls)
                .comments(comments)
                .build();
    }
}
