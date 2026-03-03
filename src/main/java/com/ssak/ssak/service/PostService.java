package com.ssak.ssak.service;

import com.ssak.ssak.domain.community.*;
import com.ssak.ssak.domain.community.dto.CommentResponse;
import com.ssak.ssak.domain.community.dto.PostListResponse;
import com.ssak.ssak.domain.community.dto.PostRequest;
import com.ssak.ssak.domain.community.dto.PostResponse;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final CommentRepository commentRepository;
    private PostRepository postRepository;

    /**
     * 해당 식당에 해당하는 게시글을 모두 반환한다.
     * @param restId
     * @return
     */
    @Transactional(readOnly = true)
    public List<PostListResponse> getPostList(Long restId) {
        List<Post> postList = postRepository.findAllByRestaurant_RestaurantId(restId);

        return postList.stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시물의 내용을 반환한다.
     * @param restId
     * @param postId
     * @return
     */
    @Transactional(readOnly = true)
    public PostResponse getPost(Long restId, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 게시글 이미지 불러오기
        List<String> postPhotos = post.getPostPhotos().stream()
                .map(PostPhoto::getPostPhotoUrl)
                .toList();

        // 게시글 댓글 불러오기
        List<Comment> comments = commentRepository.findAllByPostIdWithUser(postId);

        List<CommentResponse> commentTree = convertToDto(comments);

        // dto생성
        return PostResponse.builder()
                .postId(post.getPostId())
                .postContent(post.getPostContent())
                .postVisibility(post.isPostVisibility())
                .postCommentCnt(post.getPostCommentCnt())
                .postLikeCnt(post.getPostLikeCnt())
                .nickname(post.getUser().getUserNm())
                .postCreateTime(post.getCreatedAt())
                .comments(commentTree) // 조립된 트리 삽입!
                .imageUrls(postPhotos)
                .build();
    }

    /**
     * 댓글들을 트리구조로 변환한다. (계층형 댓글 구조 반환 위함)
     * @param comments
     * @return
     */
    public List<CommentResponse> convertToDto(List<Comment> comments) {
        // 1. 반환할 부모 댓글 리스트
        List<CommentResponse> result = new ArrayList<>();

        // 2. 부모-자식 관계 매핑을 위한 temp (Map)
        Map<Long, CommentResponse> map = new HashMap<>();

        // 3. 먼저 모든 댓글을 DTO로 반환해서 Map에 넣음
        comments.forEach(comment -> {
            CommentResponse dto = CommentResponse.builder()
                    .commentId(comment.getCommentId())
                    .commentContent(comment.getCommentContent())
                    .nickname(comment.getUser().getUserNm())
                    .commentCreateTime(comment.getCreatedAt())
                    .childrenComments(new ArrayList<>())
                    .build();
            map.put(comment.getCommentId(), dto);
        });

        // 4. 이제 하나씩 꺼내서 부모가 있으면 부모의 children에 넣고, 없으면 result에 넣음
        comments.forEach(comment -> {
            CommentResponse dto = map.get(comment.getCommentId());

            if(comment.getParent() != null) {
                // 대댓글인 경우: 부모 DTO를 찾아 그 안의 children 리스트에 나를 추가
                CommentResponse parentDto = map.get(comment.getParent().getCommentId());
                if (parentDto != null) {
                    parentDto.getChildrenComments().add(dto);
                }
            } else {
                // 최상위 댓글인 경우: 결과 리스트에 직접 추가
                result.add(dto);
            }
        });
        return result;
    }


    /**
     * 새로운 게시글을 등록한다.
     * @param request
     * @return
     */
    public List<PostResponse> createPost(Long restId, PostRequest request, String userId) {
        return null;
    }
}
