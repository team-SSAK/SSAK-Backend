package com.ssak.ssak.service;

import com.ssak.ssak.domain.measurement.Measurement;
import com.ssak.ssak.domain.measurement.MeasurementRepository;
import com.ssak.ssak.domain.measurement.dto.AIResponse;
import com.ssak.ssak.domain.measurement.dto.MeasurementResponse;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;

    /**
     * 잔반을 측정한다
     * @param file
     * @param userId
     */
    @Transactional
    public MeasurementResponse measureLeftover(MultipartFile file, Long userId) {
        // 1. 파이썬 AI 모델 서버로 이미지 전송 및 결과 수신
        AIResponse response = restTemplate.postForObject(
                "http://ai-model-service:8080/api/predict",
                file,
                AIResponse.class
        );

        // 유효성 검사
        if (response == null || response.getImageUrl() == null || response.getLeftoverRatio() == null) {
            throw new CustomException(ErrorCode.INCORRECT_RESPONSE);
        }

        String imgUrl = response.getImageUrl();
        Double leftoverRatio = response.getLeftoverRatio();

        // 2. Ratio에 따른 포인트 계산
        int addedPoints = calculatePoints(leftoverRatio);

        // 3. 사용자 포인트 업데이트
        User user = userRepository.findById(userId).orElseThrow(() ->  new CustomException(ErrorCode.USER_NOT_FOUND));
        user.addPoint(addedPoints);

        // 4. 인식 기록 저장
        Measurement measurement = Measurement.builder()
                .user(user)
                .mmPhotoUrl(imgUrl)
                .leftoverRatio(leftoverRatio)
                .build();
        measurementRepository.save(measurement);

        return MeasurementResponse.from(measurement, addedPoints, user.getCurrentPoint());
    }

    /**
     * 인식률에 따른 포인트를 계산한다
     */
    private int calculatePoints(Double leftoverRatio) {
        if (leftoverRatio == null) {
            throw new CustomException(ErrorCode.INCORRECT_RESPONSE);
        }

        double percentage = leftoverRatio * 100.0;

        if (percentage > 80 && percentage <= 100) {
            return 1000;
        } else if (percentage > 70 && percentage <= 80) {
            return 50;
        } else if (percentage > 50 && percentage <= 70) {
            return 10;
        } else {
            return 0;
        }
    }
}
