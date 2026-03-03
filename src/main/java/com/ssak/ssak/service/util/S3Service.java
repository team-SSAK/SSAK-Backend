package com.ssak.ssak.service.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 기존 이미지를 삭제한다.
     * @param imageUrl
     */
    public void deleteExistingProfileImage(String imageUrl) {
        if(imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // URL에서 객체 키 (파일명) 추출
            String splitStr = ".com/";
            String fileName = imageUrl.substring(imageUrl.indexOf(splitStr) + splitStr.length());
            amazonS3.deleteObject(bucket, fileName);
        } catch(Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
        }
    }

    /**
     * 새 파일 업로드 후 저장된 URL 반환
     * @param file
     */
    public String uploadProfileImage(MultipartFile file) {

        String folderName = "profile_img/";
//        String fileName = folderName + UUID.randomUUID() + "_" + file.getOriginalFilename();
        String fileName = folderName + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));
        } catch (AmazonServiceException e) {
            // AWS 서버 측 에러 (권한, 버킷 이름 오류 등)
            log.error("AWS 서비스 에러: {}", e.getErrorMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        } catch (IOException | SdkClientException e) {
            // 파일 읽기 실패 또는 네트워크 오류
            log.error("S3 업로드 에러: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
        return amazonS3.getUrl(bucket, fileName).toString();
    }
}
