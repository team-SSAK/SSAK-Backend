package com.ssak.ssak.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostPhotoRepository extends JpaRepository<PostPhoto, Long> {
    List<PostPhoto> findAllByPostPhotoIdInAndPostPostId(List<Long> deleteImageIds, Long postId);
}
