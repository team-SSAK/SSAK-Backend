package com.ssak.ssak.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDetailRequest {
    private String userNm;
    private boolean marketingAgreeYn;
}
