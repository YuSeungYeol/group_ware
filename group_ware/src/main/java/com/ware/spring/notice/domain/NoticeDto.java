package com.ware.spring.notice.domain;

import java.time.LocalDateTime;

import com.ware.spring.member.domain.Member;
import com.ware.spring.notice.controller.NoticeApiController;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class NoticeDto {

    private Long noticeNo;
    private String noticeTitle;
    private String noticeContent;
    private Member member;
    private LocalDateTime noticeRegDate;
    private LocalDateTime noticeNewDate;
    private int noticeView;
    
    // search 관련 필드
    private int search_type = 1;
    private String search_text;
    
    // deleteYn 필드 추가
    private String deleteYn;

    public Notice toEntity() {
        return Notice.builder()
                .noticeNo(noticeNo)
                .noticeTitle(noticeTitle)
                .noticeContent(noticeContent)
                .noticeRegDate(noticeRegDate)
                .noticeNewDate(noticeNewDate)
                .noticeView(noticeView)
                .member(member)  // Member 객체를 설정
                .deleteYn(deleteYn)  // deleteYn 필드 설정
                .build();
    }
    
    public NoticeDto toDto(Notice notice) {
        return NoticeDto.builder()
                .noticeNo(notice.getNoticeNo())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticeRegDate(notice.getNoticeRegDate())
                .noticeNewDate(notice.getNoticeNewDate())
                .noticeView(notice.getNoticeView())
                .member(notice.getMember())
                .deleteYn(notice.getDeleteYn())  // deleteYn 필드 추가
                .build();
    }
}
