package com.ssak.ssak.service;

import com.ssak.ssak.domain.Point.PointHist;
import com.ssak.ssak.domain.Point.PointHistRepository;
import com.ssak.ssak.domain.measurement.Measurement;
import com.ssak.ssak.domain.measurement.MeasurementRepository;
import com.ssak.ssak.domain.measurement.dto.AIResponse;
import com.ssak.ssak.domain.measurement.dto.MeasurementResponse;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;
    private final PointHistRepository pointHistRepository;

    /**
     * 잔반을 측정한다
     * @param file
     * @param userId
     */
    @Transactional
    public MeasurementResponse measureLeftover(MultipartFile file, Long userId) {
        // 1. RestClient를 사용한 파이썬 AI 모델 서버 통신
        RestClient restClient = RestClient.create();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource()); //파일 리소스 추가

        AIResponse response = restClient.post()
                .uri("http://ai-model-service:8000/api/predict")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(AIResponse.class);

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

        // 5. 포인트 내역에 저장
        PointHist pointHist = PointHist.savePointBuilder()
                .user(user)
                .pointAmount(addedPoints) // int 타입
                .pointDesc("잔반 인증 완료 " + (int) (leftoverRatio * 100) + "%") // String 타입
                .build();
        pointHistRepository.save(pointHist);

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
