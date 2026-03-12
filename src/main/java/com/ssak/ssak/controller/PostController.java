package com.ssak.ssak.controller;

import com.ssak.ssak.domain.community.dto.CommentRequest;
import com.ssak.ssak.domain.community.dto.PostListResponse;
import com.ssak.ssak.domain.community.dto.PostRequest;
import com.ssak.ssak.domain.community.dto.PostResponse;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.PostService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<PostListResponse>> getPostList(@PathVariable Long restId) {
        return ResponseEntity.ok(postService.getPostList(restId));
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
    @PostMapping("/{restId}")
    public ResponseEntity<PostResponse> createPost(@PathVariable Long restId, @RequestBody PostRequest postRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.createPost(restId, postRequest, userDetails.getUserId()));
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
     * 자신이 작성한 댓글을 삭제한다.
     * @param commentId
     * @param userDetails
     * @return
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.deleteComment(commentId, userDetails.getUserId()));
    }
}
