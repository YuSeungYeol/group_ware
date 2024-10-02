package com.ware.spring.schedule.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.ware.spring.member.domain.Member;

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
    private Member member;  // Member 객체 추가

    public Schedule toEntity() {
        return Schedule.builder()
                .schedule_no(schedule_no)
                .member(member)  // 로그인된 사용자의 Member 객체 설정
                .start_date(start_date)
                .end_date(end_date)
                .start_time(start_time)
                .end_time(end_time)
                .schedule_title(schedule_title)
                .schedule_content(schedule_content)
                .schedule_new_date(schedule_new_date)
                .build();
    }

    public static ScheduleDto toDto(Schedule schedule) {
        return ScheduleDto.builder()
                .schedule_no(schedule.getSchedule_no())
                .member(schedule.getMember())  // Member 반환
                .start_date(schedule.getStart_date())
                .end_date(schedule.getEnd_date())
                .start_time(schedule.getStart_time())
                .end_time(schedule.getEnd_time())
                .schedule_title(schedule.getSchedule_title())
                .schedule_content(schedule.getSchedule_content())
                .schedule_new_date(schedule.getSchedule_new_date())
                .build();
    }
    
}

