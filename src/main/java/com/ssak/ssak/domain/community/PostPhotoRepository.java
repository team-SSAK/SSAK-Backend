package com.ssak.ssak.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostPhotoRepository extends JpaRepository<PostPhoto, Long> {
    //List<PostPhoto> findAllByPostPhotoIdInAndPostPostId(List<Long> deleteImageIds, Long postId);
    Optional<List<PostPhoto>> findByPostPostId(Long postId);
}
