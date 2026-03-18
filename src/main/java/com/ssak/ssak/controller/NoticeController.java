package com.ssak.ssak.controller;

import com.ssak.ssak.domain.notice.dto.NoticeListResponse;
import com.ssak.ssak.domain.notice.dto.NoticeResponse;
import com.ssak.ssak.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;


    /**
     * 공지사항 목록을 조회한다.
     * @return
     */
    @GetMapping
    public ResponseEntity<List<NoticeListResponse>> getNoticeList() {
        return ResponseEntity.ok(noticeService.getNoticeList());
    }

    /**
     * 특정 공지사항의 내용을 조회한다.
     * @param noticeId
     * @return
     */
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }

}
