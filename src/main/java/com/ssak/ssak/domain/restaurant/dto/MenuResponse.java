package com.ssak.ssak.domain.restaurant.dto;

import com.ssak.ssak.domain.restaurant.MenuType;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuResponse {
    private Long menuId;
    private MenuType menuType;
    private List<String> menuItems;
}
