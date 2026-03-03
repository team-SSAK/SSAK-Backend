package com.ssak.ssak.domain.community.dto;

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
    private String postContent;
    private LocalDateTime postCreateTime;
    private String nickname;
    private int postLikeCnt;
    private int postCommentCnt;

    private List<String> imageUrls;
    private List<CommentResponse> comments;
}
