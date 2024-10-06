package com.ware.spring.schedule.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ware.spring.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Entity
@Getter
@Table(name = "schedule")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_no")
    private Long schedule_no;

    @ManyToOne
    @JoinColumn(name = "mem_no")
    @JsonIgnore
    private Member member;

    @Column(name = "start_date")
    private LocalDate start_date;

    @Column(name = "end_date")
    private LocalDate end_date;

    @Column(name = "start_time")    
    private LocalTime start_time;

    @Column(name = "end_time")
    private LocalTime end_time;

    @Column(name = "schedule_title", length = 20)
    private String schedule_title;

    @Column(name = "schedule_content")
    private String schedule_content;

    @Column(name = "schedule_new_date")
    @UpdateTimestamp
    private LocalDateTime schedule_new_date;

    @Column(name = "schedule_background_color", length = 7) // 추가된 필드
    private String schedule_background_color;

    @Column(name = "notification_minutes")
    private Integer notification_minutes;
    
    // 업데이트 메서드 (DTO를 통해 값 갱신)
    public void update(ScheduleDto dto) {
        this.start_date = dto.getStart_date();
        this.end_date = dto.getEnd_date();
        this.start_time = dto.getStart_time();
        this.end_time = dto.getEnd_time();
        this.schedule_title = dto.getSchedule_title();
        this.schedule_content = dto.getSchedule_content();
        this.schedule_background_color = dto.getSchedule_background_color();
        this.notification_minutes = dto.getNotification_minutes(); // 알림 시간 필드 추가
    }
    
}
