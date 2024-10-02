package com.ware.spring.workingTime.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "working_time")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int timeNo;

    @Column(nullable = false)
    private int memNo;

    // 주간 근무 시간을 datetime 타입으로 저장
    @Column(nullable = false)
    private LocalDateTime weekTime;

    // 총 근무 시간을 datetime 타입으로 저장
    @Column(nullable = false)
    private LocalDateTime totalTime;

    // DTO로 변환하는 메서드
    public WorkingTimeDto toDto() {
        return WorkingTimeDto.builder()
                .timeNo(this.timeNo)
                .memNo(this.memNo)
                .weekTime(this.weekTime)
                .totalTime(this.totalTime)
                .build();
    }
}
