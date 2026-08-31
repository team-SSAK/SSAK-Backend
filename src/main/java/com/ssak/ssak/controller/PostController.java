package com.ssak.ssak.controller;

import com.ssak.ssak.domain.community.dto.*;
import com.ssak.ssak.domain.util.dto.ReportRequest;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class PostController {
    private final PostService postService;

    /**
     * 해당 식당의 커뮤니티 게시글 목록을 반환한다.
     * @param restId
     * @return
     */
    @GetMapping("/{restId}")
    public ResponseEntity<List<PostListResponse>> getPostList(@PathVariable Long restId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.getPostList(restId, userDetails.getUserId()));
    }

    /**
     * 해당 식당의 특정 커뮤니티 게시글의 내용을 반환한다.
     * @param postId
     * @return
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    /**
     * 해당 식당의 커뮤니티에 게시글을 작성한다.
     * @param restId
     * @param postRequest
     * @param userDetails
     * @return
     */
    @PostMapping(value = "/{restId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(@PathVariable Long restId, @ModelAttribute PostRequest postRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.createPost(restId, postRequest, userDetails.getUserId()));
    }

    /**
     * 해당 게시글을 수정한다
     * @param postId
     * @param postEditRequest
     * @param userDetails
     * @return
     */
    @PatchMapping(value = "/post/{postId}", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostListResponse> editPost(@PathVariable Long postId, @ModelAttribute PostEditRequest postEditRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.editPost(postId, postEditRequest, userDetails.getUserId()));
    }

    /**
     * 특정 게시글을 좋아요 등록/취소 한다.
     * @param postLikeRequest
     * @param userDetails
     * @return
     */
    @PostMapping("/post/wish")
    public ResponseEntity<PostLikeResponse> likePost(@RequestBody PostLikeRequest postLikeRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.likePost(postLikeRequest.getLikedPostId(), userDetails.getUserId()));
    }

    /**
     * 특정 댓글을 좋아요 등록/취소 한다.
     * @param commentLikeRequest
     * @param userDetails
     * @return
     */
    @PostMapping("/comment/wish")
    public ResponseEntity<CommentLikeResponse> likeComment(@RequestBody CommentLikeRequest commentLikeRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.likeComment(commentLikeRequest.getLikedCommentId(), userDetails.getUserId()));
    }

    /**
     * 해당 식당의 커뮤니티의 특정 게시글에 댓글을 작성한다.
     * @param postId
     * @return
     */
    @PostMapping("/post/{postId}")
    public ResponseEntity<String> createComment(@PathVariable Long postId, @RequestBody CommentRequest commentRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.createComment(postId, commentRequest, userDetails.getUserId()));
    }

    /**
     * 자신이 작성한 게시물을 삭제한다.
     * @param postId
     * @param userDetails
     * @return
     */
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.deletePost(postId, userDetails.getUserId()));
    }

    /**
     * 자신이 작성한 댓글 내용을 수정한다.
     * @param commentId
     * @param request
     * @param userDetails
     * @return
     */
    @PatchMapping("/comment/{commentId}")
    public ResponseEntity<CommentResponse> editComment(@PathVariable Long commentId, @RequestBody CommentEditRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.editComment(commentId, request, userDetails.getUserId()));
    }

    /**
     * 자신이 작성한 댓글을 삭제한다.
     * @param commentId
     * @param userDetails
     * @return
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.deleteComment(commentId, userDetails.getUserId()));
    }

    /**
     * 특정 게시글을 신고한다.
     * @param postId
     * @param request
     * @param userDetails
     * @return
     */
    @PostMapping("/post/{postId}/report")
    public ResponseEntity<String> reportPost(@PathVariable Long postId, @RequestBody ReportRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.reportPost(postId, request, userDetails.getUserId()));
    }

    /**
     * 특정 댓글을 신고한다.
     * @param commentId
     * @param request
     * @param userDetails
     * @return
     */
    @PostMapping("/comment/{commentId}/report")
    public ResponseEntity<String> reportComment(@PathVariable Long commentId, @RequestBody ReportRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.reportComment(commentId, request, userDetails.getUserId()));
    }
}
