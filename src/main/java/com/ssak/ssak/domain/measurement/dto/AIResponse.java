package com.ssak.ssak.domain.measurement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {
    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("leftover_ratio")
    private Double leftoverRatio;
}