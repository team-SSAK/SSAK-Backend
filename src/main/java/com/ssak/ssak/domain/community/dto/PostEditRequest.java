package com.ssak.ssak.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostEditRequest {
    private String postTitle;
    private String postContent;
    private Boolean postVisibility;

    private List<Long> deleteImageIds;          // 삭제 필요한 이미지 ID
    private List<MultipartFile> newImages;      // 새로운 이미지 ID
}
