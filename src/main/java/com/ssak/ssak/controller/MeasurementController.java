package com.ssak.ssak.controller;

import com.ssak.ssak.domain.measurement.dto.MeasurementResponse;
import com.ssak.ssak.domain.measurement.dto.MeasurementValidResponse;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.MeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    /**
     * 잔반을 측정한다
     * @param userDetails
     * @param file
     * @return
     */
    @PostMapping(value ="/measure", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MeasurementResponse> measureLeftover(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                               @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(measurementService.measureLeftover(file, userDetails.getUserId()));
    }

    /**
     * 사용자가 잔반 인식이 가능한 상태인지 확인합니다.
     * @param userDetails
     * @return
     */
    @GetMapping("/measure/valid")
    public ResponseEntity<MeasurementValidResponse> isvalidMeasurement(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(measurementService.validateMeasurement(userDetails.getUserId()));
    }
}
