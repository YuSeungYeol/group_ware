package com.ware.spring.workingTime.service;

import com.ware.spring.workingTime.domain.WorkingTime;
import com.ware.spring.workingTime.repository.WorkingTimeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WorkingTimeService {

    private final WorkingTimeRepository workingTimeRepository;

    public WorkingTimeService(WorkingTimeRepository workingTimeRepository) {
        this.workingTimeRepository = workingTimeRepository;
    }

    // 출근 시 주간 시간 및 총 근무 시간 초기화
    public void startWork(int memNo) {
        WorkingTime workingTime = workingTimeRepository.findByMemNo(memNo);
        if (workingTime == null) {
            workingTime = WorkingTime.builder()
                    .memNo(memNo)
                    .weekTime(LocalDateTime.now())  // 주간 시간 초기화
                    .totalTime(LocalDateTime.now())  // 총 근무 시간 초기화
                    .build();
        } else {
            // 이미 존재하는 경우 주간 근무 시간만 초기화
            workingTime.setWeekTime(LocalDateTime.now());
        }
        workingTimeRepository.save(workingTime);
    }

    // 퇴근 시 근무 시간 계산 및 업데이트
    public void endWork(int memNo, LocalDateTime endTime) {
        WorkingTime workingTime = workingTimeRepository.findByMemNo(memNo);
        if (workingTime != null) {
            LocalDateTime startTime = workingTime.getWeekTime();
            if (startTime != null) {
                // 주간 근무 시간 계산
                long hoursWorked = java.time.Duration.between(startTime, endTime).toHours();
                long minutesWorked = java.time.Duration.between(startTime, endTime).toMinutes() % 60;
                System.out.println("근무 시간: " + hoursWorked + "시간 " + minutesWorked + "분");

                // 주간 및 총 근무 시간 업데이트
                workingTime.setWeekTime(endTime);  // 주간 시간 업데이트
                workingTime.setTotalTime(endTime); // 총 시간 업데이트
                workingTimeRepository.save(workingTime);
            }
        }
    }
}
