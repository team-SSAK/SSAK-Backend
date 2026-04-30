package com.ssak.ssak.domain.measurement.dto;

import com.ssak.ssak.domain.measurement.Measurement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeasurementResponse {
    private Double leftoverRatio;
    private int addedPoint;
    private int currentPoint;

    public static MeasurementResponse from(Measurement measurement, int addedPoint, int currentPoint) {
        return MeasurementResponse.builder()
                .leftoverRatio(measurement.getLeftoverRatio())
                .addedPoint(addedPoint)
                .currentPoint(currentPoint)
                .build();
    }
}
