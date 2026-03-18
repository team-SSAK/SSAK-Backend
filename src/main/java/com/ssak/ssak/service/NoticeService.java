package com.ssak.ssak.service;

import com.ssak.ssak.domain.notice.Notice;
import com.ssak.ssak.domain.notice.NoticeRepository;
import com.ssak.ssak.domain.notice.dto.NoticeListResponse;
import com.ssak.ssak.domain.notice.dto.NoticeResponse;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    /**
     * 공지사항 목록을 조회한다.
     * @return
     */
    @Transactional(readOnly = true)
    public List<NoticeListResponse> getNoticeList() {
        List<Notice> noticeList = noticeRepository.findAllByOrderByUpdatedAtDesc();
        return noticeList.stream()
                .map(NoticeListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 해당 공지사항의 내용을 조회한다.
     * @param noticeId
     * @return
     */
    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        return NoticeResponse.from(notice);
    }
}
