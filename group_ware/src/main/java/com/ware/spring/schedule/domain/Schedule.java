package com.ware.spring.schedule.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.UpdateTimestamp;

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
    public void update(ScheduleDto dto) {
        this.start_date = dto.getStart_date();
        this.end_date = dto.getEnd_date();
        this.start_time = dto.getStart_time();
        this.end_time = dto.getEnd_time();
        this.schedule_title = dto.getSchedule_title();
        this.schedule_content = dto.getSchedule_content();
    }
    
}
