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
    private String authorProfileImg;
    private Boolean isOwner;
    private LocalDateTime postCreateTime;
    private int postLikeCnt;
    private int postCommentCnt;

    public static PostListResponse from(Post post) {
        String userNm = null;
        String profileImg = null;
        boolean isOwner = false;

        if (post.getUser() != null) {
            userNm = post.getUser().getUserNm();
            profileImg = post.getUser().getUserProfileImg();
            isOwner = post.getUser().getUserRole() == UserRole.OWNER;
        }
        return PostListResponse.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postContent(post.getPostContent())
                .postVisibility(post.getPostVisibility())
                .nickname(userNm)
                .authorProfileImg(profileImg)
                .isOwner(isOwner)
                .postCreateTime(post.getCreatedAt())
                .postLikeCnt(post.getPostLikeCnt())
                .postCommentCnt(post.getPostCommentCnt())
                .build();
    }
}
