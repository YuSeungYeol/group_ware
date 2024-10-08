package com.ware.spring.schedule.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ware.spring.member.domain.Member;
import com.ware.spring.notice.domain.Notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDto {

    private Long schedule_no;
    private LocalDate start_date;
    private LocalDate end_date;
    private LocalTime start_time;
    private LocalTime end_time;
    private String schedule_title;
    private String schedule_content;
    private LocalDateTime schedule_new_date;
    private String schedule_background_color; // 추가된 필드
    private Integer notification_minutes; // 알림 시간 필드 추가
    private Member member;
    private Notice notice; // 공지사항 추가
    @JsonProperty("is_notice")
    private boolean is_notice; // 공지사항 여부 필드 추가

    // DTO를 엔티티로 변환하는 메서드
    public Schedule toEntity() {
        return Schedule.builder()
                .member(member)
                .start_date(start_date)
                .end_date(end_date)
                .start_time(start_time)
                .end_time(end_time)
                .schedule_title(schedule_title)
                .schedule_content(schedule_content)
                .schedule_background_color(schedule_background_color) // 배경색 필드 추가
                .notification_minutes(notification_minutes) // 알림 시간 필드 추가
                .build();
    }

    // 엔티티를 DTO로 변환하는 메서드
    public static ScheduleDto toDto(Schedule schedule) {
        return ScheduleDto.builder()
                .schedule_no(schedule.getSchedule_no())
                .member(schedule.getMember())
                .start_date(schedule.getStart_date())
                .end_date(schedule.getEnd_date())
                .start_time(schedule.getStart_time())
                .end_time(schedule.getEnd_time())
                .schedule_title(schedule.getSchedule_title())
                .schedule_content(schedule.getSchedule_content())
                .schedule_background_color(schedule.getSchedule_background_color()) // 배경색 필드 추가
                .notification_minutes(schedule.getNotification_minutes()) // 알림 시간 필드 추가
                .schedule_new_date(schedule.getSchedule_new_date())
                .is_notice(false) // 일반 일정일 경우 false로 설정
                .build();
    }
}
