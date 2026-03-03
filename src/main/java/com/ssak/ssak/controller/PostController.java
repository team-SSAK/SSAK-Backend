package com.ssak.ssak.controller;

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
     * 해당 식당의 커뮤니티 게시글을 반환한다.
     * @param restId
     * @return
     */
    @GetMapping("/{restId}")
    public ResponseEntity<List<PostListResponse>> getPostList(@PathVariable Long restId) {
        return ResponseEntity.ok(postService.getPostList(restId));
    }

    /**
     * 해당 식당의 특정 커뮤니티 게시글의 내용을 반환한다.
     * @param restId
     * @param postId
     * @return
     */
    @GetMapping("/{restId}/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long restId, @PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(restId, postId));
    }

//    @PostMapping("/{restId}")
//    public ResponseEntity<PostResponse> createPost(@PathVariable Long restId, @RequestBody PostRequest postRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
//        return ResponseEntity.ok(postService.createPost(restId, postRequest, userDetails.getUserId()));
//    }
}
