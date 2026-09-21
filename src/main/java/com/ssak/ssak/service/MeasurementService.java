package com.ssak.ssak.service;

import com.ssak.ssak.domain.Point.PointHist;
import com.ssak.ssak.domain.Point.PointHistRepository;
import com.ssak.ssak.domain.measurement.Measurement;
import com.ssak.ssak.domain.measurement.MeasurementRepository;
import com.ssak.ssak.domain.measurement.dto.MeasurementResponse;
import com.ssak.ssak.domain.measurement.dto.MeasurementValidResponse;
import com.ssak.ssak.domain.restaurant.Restaurant;
import com.ssak.ssak.domain.restaurant.RestaurantRepository;
import com.ssak.ssak.domain.user.User;
import com.ssak.ssak.domain.user.UserRepository;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import com.ssak.ssak.service.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final GptVisionService gptVisionService;
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;
    private final PointHistRepository pointHistRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * 잔반을 측정한다
     * @param file
     * @param userId
     */
    @Transactional
    public MeasurementResponse measureLeftover(MultipartFile file, Long userId,
                                               Long restaurantId, Double latitude, Double longitude) {
        // 0. 횟수·쿨다운 검증 (프론트 우회 방지)
        assertMeasurementAllowed(userId);

        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INCORRECT_IMAGE);
        }

        try {
            // 1. GPT Vision으로 잔반 비율 분석
            double leftoverRatio = gptVisionService.analyzeLeftoverRatio(file);
            if (leftoverRatio < 0) {
                throw new CustomException(ErrorCode.INCORRECT_IMAGE);
            }

            // 2. S3에 원본 이미지 업로드 (기록용)
            String imgUrl = s3Service.uploadSingleImage(file, "measurement");

            // 2. Ratio에 따른 포인트 계산
            //int addedPoints = calculatePoints(leftoverRatio);
            int addedPoints = 100; //TODO: 축제기간 동안 100포인트로 고정 - 추후 수정 필요

            // 3. 사용자 포인트 업데이트
            User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            user.addPoint(addedPoints);

            // 4. 식당 반경 검증 및 식당 엔티티 조회
            Restaurant restaurant = null;
            if (restaurantId != null) {
                restaurant = restaurantRepository.findById(restaurantId)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));

                if (latitude != null && longitude != null && restaurant.getRestaurantCoord() != null) {
                    double distance = haversineDistance(latitude, longitude,
                            restaurant.getRestaurantCoord().getY(),  // latitude (Y in SRID 4326)
                            restaurant.getRestaurantCoord().getX()); // longitude (X in SRID 4326)
                    if (distance > 100) {
                        throw new CustomException(ErrorCode.OUT_OF_RESTAURANT_RANGE);
                    }
                }
            }

            // 5. 인식 기록 저장
            Measurement measurement = Measurement.builder()
                    .user(user)
                    .mmPhotoUrl(imgUrl)
                    .leftoverRatio(leftoverRatio)
                    .restaurant(restaurant)
                    .shotLat(latitude)
                    .shotLon(longitude)
                    .build();
            measurementRepository.save(measurement);

            // 6. 포인트 내역에 저장
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
     * 잔반 인증 가능 여부를 검증한다. 불가 시 예외를 던진다.
     */
    private void assertMeasurementAllowed(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();

        int todayCount = measurementRepository.countByUserUserIdAndCreatedAtAfter(userId, startOfDay);
        if (todayCount >= 3) {
            throw new CustomException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        Optional<Measurement> last = measurementRepository.findFirstByUserUserIdOrderByCreatedAtDesc(userId);
        if (last.isPresent() && last.get().getCreatedAt().plusHours(4).isAfter(now)) {
            throw new CustomException(ErrorCode.COOL_DOWN_PERIOD_LEFT);
        }
    }

    /**
     * Haversine 공식으로 두 좌표 사이의 거리(m)를 계산한다
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
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
