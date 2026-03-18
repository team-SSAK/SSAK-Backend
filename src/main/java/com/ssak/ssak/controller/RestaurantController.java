package com.ssak.ssak.controller;

import com.ssak.ssak.domain.restaurant.dto.*;
import com.ssak.ssak.security.CustomUserDetails;
import com.ssak.ssak.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    /**
     * 전체 식당 목록을 조회한다.
     * @return
     */
    @GetMapping
    public ResponseEntity<List<RestaurantListResponse>> getRestaurantList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(restaurantService.getRestaurantList(userDetails.getUserId()));
    }

    /**
     * 특정 식당 정보를 조회한다.
     * @param restId
     * @return
     */
    @GetMapping("/{restId}")
    public ResponseEntity<RestaurantResponse> getRestaurantDetail(@PathVariable Long restId) {
        return ResponseEntity.ok(restaurantService.getRestaurantDetail(restId));
    }

    /**
     * 특정 사용자의 식당 찜 목록을 조회한다.
     * @param userDetails
     * @return
     */
    @GetMapping("/wish")
    public ResponseEntity<List<RestaurantWishResponse>> getMyWishRestaurantList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(restaurantService.getMyWishRestaurantList(userDetails.getUserId()));
    }

    /**
     * 특정 사용자가 특정 식당을 찜하거나 취소한다.
     */
    @PostMapping("/wish")
    public ResponseEntity<RestaurantWishActionResponse> updateRestaurantWishStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                   @RequestBody RestaurantWishRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurantWishStatus(userDetails.getUserId(), request.getWishRestaurantId()));
    }

    /**
     * 특정 식당의 오늘의 메뉴 목록을 조회한다
     * @param restId
     * @return
     */
    @GetMapping("/{restId}/menu")
    public ResponseEntity<List<MenuResponse>> getTodayMenu(@PathVariable Long restId) {
        return  ResponseEntity.ok(restaurantService.getTodayMenu(restId));
    }
}
