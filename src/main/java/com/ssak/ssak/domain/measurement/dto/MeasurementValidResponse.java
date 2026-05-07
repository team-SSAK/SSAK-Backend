package com.ssak.ssak.domain.measurement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeasurementValidResponse {
    private Boolean isValid;
    private LocalDateTime lastMeasuredDate;
}
