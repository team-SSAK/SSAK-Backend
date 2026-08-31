package com.ssak.ssak.service;

import com.ssak.ssak.domain.community.*;
import com.ssak.ssak.domain.community.dto.*;
import com.ssak.ssak.domain.restaurant.Restaurant;
import com.ssak.ssak.domain.restaurant.RestaurantRepository;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.domain.user.UserRole;
import com.ssak.ssak.domain.util.Report;
import com.ssak.ssak.domain.util.ReportRepository;
import com.ssak.ssak.domain.util.ReportType;
import com.ssak.ssak.domain.util.dto.ReportRequest;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import com.ssak.ssak.service.util.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.tool.schema.TargetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final CommentRepository commentRepository;
    private final S3Service s3Service;
    private final PostRepository postRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final PostLikeRepository postLikeRepository;
    private final ReportRepository reportRepository;
    private final CommentLikeRepository commentLikeRepository;

    /**
     * 해당 식당에 해당하는 게시글을 모두 반환한다.
     * @param restId
     * @return
     */
    @Transactional(readOnly = true)
    public List<PostListResponse> getPostList(Long restId, Long userId) {
        List<Post> postList = postRepository.findAllByRestaurant_RestaurantId(restId);

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserRole() == UserRole.OWNER || user.getUserRole() == UserRole.ADMIN) {
            return postList.stream()
                    .map(PostListResponse::from)
                    .collect(Collectors.toList());
        }

        return postList.stream()
                .filter(post -> Boolean.TRUE.equals(post.getPostVisibility()))
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
                .postVisibility(request.getPostVisibility())
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
     * 작성한 게시글을 수정한다.
     * @param postId
     * @param request
     * @param userId
     * @return
     */
    @Transactional
    public PostListResponse editPost(Long postId, PostEditRequest request, Long userId) {

        // 1. 기존의 post 찾기
        Post post =  postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_POST_OWNER);
        }

        // 2. 변경사항 저장
        post.editPost(request.getPostTitle(), request.getPostContent(), request.getPostVisibility());

        // 3. 이미지 변경사항 있을 경우
            // 삭제
//        if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
//
//            List<PostPhoto> photos = postPhotoRepository.findAllByPostPhotoIdInAndPostPostId(request.getDeleteImageIds(), post.getPostId());
//
//            if (photos.size() != request.getDeleteImageIds().size()) {
//                throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
//            }
//
//            for (PostPhoto photo : photos) {
//                s3Service.deleteExistingProfileImage(photo.getPostPhotoUrl());
//            }
//            postPhotoRepository.deleteAll(photos);
//
//        }
        // 기존의 post와 관련된 이미지 모두 삭제 - 다시 추가하도록
        Optional<List<PostPhoto>> postPhotos = postPhotoRepository.findByPostPostId(post.getPostId());
        postPhotos.ifPresent(photos -> {
            // 1. S3 이미지 삭제
            for (PostPhoto photo : photos) {
                s3Service.deleteExistingProfileImage(photo.getPostPhotoUrl());
            }
            // 2. DB 데이터 삭제
            postPhotoRepository.deleteAll(photos);
        });

        // 추가
        if(request.getNewImages() != null && !request.getNewImages().isEmpty()) {
            List<String> images = s3Service.uploadImages(request.getNewImages(), "post");

            // 3. PostPhoto(DB)에 사진 저장
            List<PostPhoto> newPostPhotos = images.stream()
                    .map(url -> PostPhoto.builder()
                            .post(post)
                            .postPhotoUrl(url)
                            .build())
                    .toList();

            postPhotoRepository.saveAll(newPostPhotos);
        }

        return PostListResponse.from(post);
    }

    /**
     * 특정 게시물에 대한 좋아요를 등록/취소한다.
     * @param postId
     * @param userId
     * @return
     */
    @Transactional
    public PostLikeResponse likePost(Long postId, Long userId) {
        // 1. 기존에 좋아요 내역이 있는 Post인지 조회
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        Optional<PostLike> postLike = postLikeRepository.findByUser_UserIdAndPost_PostId(userId, postId);

        // 2. 찜 내역이 존재한다면, 찜 취소
        if (postLike.isPresent()) {
            postLikeRepository.delete(postLike.get());
            post.deleteLiked();
            return new PostLikeResponse(false, postId);
        } else {
            User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            postLikeRepository.save(PostLike.createPostLike(user, post));
            post.addLiked();
            return new PostLikeResponse(true, postId);
        }
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
     * 특정 댓글에 대한 좋아요를 등록/취소한다.
     * @param commentId
     * @param userId
     * @return
     */
    @Transactional
    public CommentLikeResponse likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Optional<CommentLike> commentLike = commentLikeRepository.findByUser_UserIdAndComment_CommentId(userId, commentId);

        if (commentLike.isPresent()) {
            commentLikeRepository.delete(commentLike.get());
            comment.deleteLiked();
            return new CommentLikeResponse(false, commentId);
        } else {
            commentLikeRepository.save(CommentLike.createCommentLike(user, comment));
            comment.addLiked();
            return new CommentLikeResponse(true, commentId);
        }
    }

    /**
     * 자신이 작성한 댓글 내용을 수정한다.
     * @param commentId
     * @param request
     * @param userId
     * @return
     */
    @Transactional
    public CommentResponse editComment(Long commentId, CommentEditRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!comment.getUser().equals(user)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }

        comment.editComment(request.getCommentContent());
        return CommentResponse.from(comment);
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

        if(!comment.getUser().equals(user)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }
        commentRepository.delete(comment);

        Post post = comment.getPost();
        post.deleteComment();

        return "댓글이 성공적으로 삭제되었습니다.";
    }

    /**
     * 특정 게시물을 신고한다.
     * @param postId
     * @param request
     * @param userId
     * @return
     */
    @Transactional
    public String reportPost(Long postId, ReportRequest request, Long userId) {
        Post post =  postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if(!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 중복 신고 방지
        if(reportRepository.existsByReporterIdAndTargetIdAndTargetType(userId, postId, ReportType.POST)) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }

        Report report = Report.builder()
                .reporterId(userId)
                .reportContent(request.getReportContent())
                .targetId(postId)
                .targetType(ReportType.POST)
                .build();
        reportRepository.save(report);

        // 신고 3회 이상 당한 경우 블라인드 처리
        int reportedCnt = reportRepository.countByTargetIdAndTargetType(postId, ReportType.POST);

        if(reportedCnt >= 3) {
            post.changeVisibility(false);
        }

        return "신고가 정상적으로 접수되었습니다.";
    }

    /**
     * 특정 댓글을 신고한다.
     * @param commentId
     * @param request
     * @param userId
     * @return
     */
    @Transactional
    public String reportComment(Long commentId, ReportRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        if(!userRepository.existsById(userId)) {
            throw new  CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if(reportRepository.existsByReporterIdAndTargetIdAndTargetType(userId, commentId, ReportType.COMMENT)) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }

        Report report = Report.builder()
                .reporterId(userId)
                .reportContent(request.getReportContent())
                .targetId(commentId)
                .targetType(ReportType.COMMENT)
                .build();
        reportRepository.save(report);

        int reportedCnt = reportRepository.countByTargetIdAndTargetType(commentId, ReportType.COMMENT);

        if(reportedCnt >= 3) {
            comment.changeVisibility(false);
        }

        return "신고가 정상적으로 접수되었습니다.";
    }
}
