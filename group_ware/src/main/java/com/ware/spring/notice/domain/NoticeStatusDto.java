package com.ware.spring.notice.domain;

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
public class NoticeStatusDto {

	private Long noticeStatusNo;
    private Notice notice;
    private Member member;
    private String isRead;

    public NoticeStatus toEntity() {
        return NoticeStatus.builder()
                .noticeStatusNo(noticeStatusNo)
                .notice(notice)
                .member(member)
                .isRead(isRead)
                .build();
    }

    public static NoticeStatusDto fromEntity(NoticeStatus noticeStatus) {
        return NoticeStatusDto.builder()
                .noticeStatusNo(noticeStatus.getNoticeStatusNo())
                .notice(noticeStatus.getNotice())
                .member(noticeStatus.getMember())
                .isRead(noticeStatus.getIsRead())
                .build();
    }
}
