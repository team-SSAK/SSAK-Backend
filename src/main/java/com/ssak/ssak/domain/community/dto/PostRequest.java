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
public class PostRequest {
    private Boolean postVisibility;
    private String postTitle;
    private String postContent;
    private List<MultipartFile> images;
}
