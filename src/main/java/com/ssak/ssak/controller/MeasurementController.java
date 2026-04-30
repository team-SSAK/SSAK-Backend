package com.ssak.ssak.controller;

import com.ssak.ssak.domain.measurement.dto.MeasurementResponse;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.MeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    @PostMapping(value ="/measure", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MeasurementResponse> measureLeftover(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                               @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(measurementService.measureLeftover(file, userDetails.getUserId()));
    }
}
