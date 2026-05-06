package com.ssak.ssak.domain.community.dto;

import com.ssak.ssak.domain.community.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long commentId;
    private String commentContent;
    private Boolean commentVisibility;
    private String nickname;
    private LocalDateTime commentCreateTime;

    // 대댓글 리스트를 담을 필드
    private List<CommentResponse> childrenComments;

    public static CommentResponse from(Comment comment) {

        String nickname = null;
        if (comment.getUser() != null) {
            nickname = comment.getUser().getUserNm();
        }

        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .commentContent(comment.getCommentContent())
                .commentVisibility(comment.getCommentVisibility())
                .nickname(nickname)
                .commentCreateTime(comment.getCreatedAt())
                .childrenComments(comment.getChildren().stream()
                        .map(CommentResponse::from)
                        .toList())
                .build();
    }
}
