package com.ssak.ssak.domain.measurement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {
    private String imageUrl;
    private Double leftoverRatio;
}