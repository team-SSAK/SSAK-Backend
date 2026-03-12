package com.ssak.ssak.service;

import com.ssak.ssak.domain.community.*;
import com.ssak.ssak.domain.community.dto.*;
import com.ssak.ssak.domain.restaurant.Restaurant;
import com.ssak.ssak.domain.restaurant.RestaurantRepository;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import com.ssak.ssak.service.util.S3Service;
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
    private final S3Service s3Service;
    private final PostRepository postRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final PostPhotoRepository postPhotoRepository;

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
     * @param postId
     * @return
     */
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 게시글 이미지 불러오기
        List<String> postPhotos = post.getPostPhotos().stream()
                .map(PostPhoto::getPostPhotoUrl)
                .toList();

        // 게시글 댓글 불러오기
        List<Comment> comments = commentRepository.findAllByPostIdWithUser(postId);

        List<CommentResponse> commentTree = convertToDto(comments);

        // dto생성
        return PostResponse.from(post, postPhotos, commentTree);
    }

    /**
     * 댓글들을 트리구조로 변환한다. (계층형 댓글 구조 반환 위함)
     * @param comments
     * @return
     */
    public List<CommentResponse> convertToDto(List<Comment> comments) {
        return comments.stream()
                .filter(comment -> comment.getParent() == null) // 최상위 부모 댓글만 필터링
                .map(CommentResponse::from) // 여기서 재귀적으로 자식들까지 다 변환됨
                .toList();
    }


    /**
     * 새로운 게시글을 등록한다.
     * @param request
     * @return
     */
    @Transactional
    public PostResponse createPost(Long restId, PostRequest request, Long userId) {

        // 1. DB에 저장
        Restaurant restaurant = restaurantRepository.findById(restId).orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .postTitle(request.getPostTitle())
                .postContent(request.getPostContent())
                .restaurant(restaurant)
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);

        // 2. S3에 사진 업로드
        List<String> images = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            images = s3Service.uploadImages(request.getImages(), "post");

            // 3. PostPhoto(DB)에 사진 저장
            List<PostPhoto> postPhotos = images.stream()
                    .map(url -> PostPhoto.builder()
                            .post(savedPost)
                            .postPhotoUrl(url)
                            .build())
                    .toList();

            postPhotoRepository.saveAll(postPhotos);
        }
        List<CommentResponse> comments = new ArrayList<>();

        return PostResponse.from(savedPost, images, comments);
    }

    /**
     * 해당 식당의 커뮤니티의 특정 게시글에 댓글을 작성한다.
     * @param postId
     * @return
     */
    @Transactional
    public String createComment(Long postId, CommentRequest request, Long userId) {
        // 1. 조회
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 대댓글 여부 확인
        Comment parentComment = null;
        if(request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId()).orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        }

        // 3. 댓글 생성
        Comment comment = Comment.builder()
                .commentContent(request.getCommentContent())
                .user(user)
                .post(post)
                .parent(parentComment)
                .build();

        commentRepository.save(comment);

        // 4. post의 댓글 갯수 1개 증가
        post.addComment();

        return "댓글이 성공적으로 작성되었습니다.";
    }

    /**
     * 자신이 작성한 게시물을 삭제한다.
     * @param postId
     * @param userId
     * @return
     */
    @Transactional
    public String deletePost(Long postId, Long userId) {
        //TODO: cascade 확인하기 - post-comment

        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!post.getUser().equals(user)) {
            throw new CustomException(ErrorCode.NOT_POST_OWNER);
        }

        postRepository.delete(post);
        return "게시글이 성공적으로 삭제되었습니다.";
    }

    /**
     * 자신이 작성한 댓글을 삭제한다.
     * @param commentId
     * @param userId
     * @return
     */
    @Transactional
    public String deleteComment(Long commentId, Long userId) {
        //TODO: 그 밑에 댓글이 달린 댓글이 삭제될경우

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!comment.getPost().getUser().equals(user)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }
        commentRepository.delete(comment);

        Post post = comment.getPost();
        post.deleteComment();

        return "댓글이 성공적으로 삭제되었습니다.";
    }
}
