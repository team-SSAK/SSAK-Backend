package com.ssak.ssak.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    private boolean postVisibility;
    private String postContent;
    private List<MultipartFile> images;
}
