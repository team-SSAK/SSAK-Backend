package com.ssak.ssak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssak.ssak.domain.Point.PointHist;
import com.ssak.ssak.domain.Point.PointHistRepository;
import com.ssak.ssak.domain.measurement.Measurement;
import com.ssak.ssak.domain.measurement.MeasurementRepository;
import com.ssak.ssak.domain.measurement.dto.AIResponse;
import com.ssak.ssak.domain.measurement.dto.MeasurementResponse;
import com.ssak.ssak.domain.measurement.dto.MeasurementValidResponse;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

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
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INCORRECT_IMAGE);
        }

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000);  // 연결 타임아웃 10초
            factory.setReadTimeout(60000);    // 읽기 타임아웃 1분

            RestClient restClient = RestClient.builder()
                    .requestFactory(factory)
                    .build();

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource())
                    .filename(file.getOriginalFilename())
                    .contentType(MediaType.parseMediaType(file.getContentType()));

            String rawResponse = restClient.post()
                    .uri("http://ai-model-service:8000/api/predict")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();
            AIResponse response = mapper.readValue(rawResponse, AIResponse.class); //직접 파싱

            // 유효성 검사
            if (response == null || response.getLeftoverRatio() == null) {
                throw new CustomException(ErrorCode.INCORRECT_RESPONSE);    
            }            
            Double leftoverRatio = response.getLeftoverRatio();            
            // 식판 인식 실패 / 아무 사진    
            if (leftoverRatio < 0) {            
                throw new CustomException(ErrorCode.INCORRECT_IMAGE);       
            }            
            if (response.getImageUrl() == null) {            
                throw new CustomException(ErrorCode.INCORRECT_RESPONSE);           
            }          
            String imgUrl = response.getImageUrl();

            // 2. Ratio에 따른 포인트 계산
            //int addedPoints = calculatePoints(leftoverRatio);
            int addedPoints = 100; //TODO: 축제기간 동안 100포인트로 고정 - 추후 수정 필요

            // 3. 사용자 포인트 업데이트
            User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            user.addPoint(addedPoints);

            // 4. 인식 기록 저장
            Measurement measurement = Measurement.builder()
                    .user(user)
                    .mmPhotoUrl(imgUrl)
                    .leftoverRatio(leftoverRatio)
                    .build();
            measurementRepository.save(measurement);

            // 5. 포인트 내역에 저장
            PointHist pointHist = PointHist.savePoint(
                    user,
                    addedPoints,
                    "잔반 인증 완료 " + (int) (leftoverRatio * 100) + "%"
            );
            pointHistRepository.save(pointHist);

            return MeasurementResponse.from(measurement, addedPoints, user.getCurrentPoint());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 인식률에 따른 포인트를 계산한다
     */
    private int calculatePoints(Double leftoverRatio) {
        if (leftoverRatio == null) {
            throw new CustomException(ErrorCode.INCORRECT_RESPONSE);
        }

        double percentage = leftoverRatio * 100.0;

        if (percentage >= 85 && percentage <= 100) {
            return 100;
        } else if (percentage > 50 && percentage < 85) {
            return (int)percentage;
        } else if (percentage > 10 && percentage <= 50) {
            return (int) (percentage / 2);
        } else {
            return 1;
        }
    }

    /**
     * 인증할 수 있는지 여부를 확인합니다.
     * @param userId
     * @return
     */
    @Transactional
    public MeasurementValidResponse validateMeasurement(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();

        // 1. 오늘 인증 횟수 모두 소진했는지 확인
        int todayCount = measurementRepository.countByUserUserIdAndCreatedAtAfter(userId, startOfDay);
        if (todayCount >= 3) {
            throw new CustomException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        // 2. 마지막 인증 후 4시간 이상 지났는지 확인
        LocalDateTime lastMeasuredDate = measurementRepository.findFirstByUserUserIdOrderByCreatedAtDesc(userId)
                .map(Measurement::getCreatedAt)
                .orElse(null);
        boolean isCoolingDown = (lastMeasuredDate != null) && lastMeasuredDate.plusHours(4).isAfter(now);
        return MeasurementValidResponse.builder()
                .isValid(!isCoolingDown)
                .lastMeasuredDate(lastMeasuredDate)
                .build();
    }
}
