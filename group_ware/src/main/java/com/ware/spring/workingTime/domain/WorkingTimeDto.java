package com.ware.spring.workingTime.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingTimeDto {

    private int timeNo;
    private int memNo;
    private LocalDateTime weekTime;  // 주간 근무 시간
    private LocalDateTime totalTime;  // 총 근무 시간

    // 엔티티로 변환하는 메서드
    public WorkingTime toEntity() {
        return WorkingTime.builder()
                .timeNo(this.timeNo)
                .memNo(this.memNo)
                .weekTime(this.weekTime)
                .totalTime(this.totalTime)
                .build();
    }
}
