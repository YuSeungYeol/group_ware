package com.ware.spring.notice.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ware.spring.member.domain.Member;
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
    private LocalDateTime noticeRegDate;  // 등록일은 LocalDateTime으로 유지
    private LocalDateTime noticeNewDate;  // 수정일은 LocalDateTime으로 유지
    private int noticeView;
    
    // 검색 관련 필드
    private int search_type = 1;
    private String search_text;
    
    // 삭제 여부 관련 필드
    private String deleteYn;
    
    // 공지 기간 설정 관련 필드
    private LocalDate noticeStartDate;  // LocalDate로 변경
    private LocalDate noticeEndDate;    // LocalDate로 변경
    private String noticeSchedule;      // 'Y' 또는 'N' 값을 저장하는 필드

    // 엔티티로 변환하는 메서드
    public Notice toEntity() {
        return Notice.builder()
                .noticeNo(noticeNo)
                .noticeTitle(noticeTitle)
                .noticeContent(noticeContent)
                .noticeRegDate(noticeRegDate)
                .noticeNewDate(noticeNewDate)
                .noticeView(noticeView)
                .member(member)
                .deleteYn(deleteYn) 
                .noticeStartDate(noticeStartDate)  
                .noticeEndDate(noticeEndDate)      
                .noticeSchedule(noticeSchedule)    
                .build();
    }
  
    // 엔티티에서 DTO로 변환하는 메서드
    public NoticeDto toDto(Notice notice) {
        return NoticeDto.builder()
                .noticeNo(notice.getNoticeNo())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticeRegDate(notice.getNoticeRegDate())
                .noticeNewDate(notice.getNoticeNewDate())
                .noticeView(notice.getNoticeView())
                .member(notice.getMember())
                .deleteYn(notice.getDeleteYn())
                .noticeStartDate(notice.getNoticeStartDate())  
                .noticeEndDate(notice.getNoticeEndDate())      
                .noticeSchedule(notice.getNoticeSchedule())    
                .build();
    }
}
