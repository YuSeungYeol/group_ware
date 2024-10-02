package com.ware.spring.schedule.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ware.spring.schedule.domain.Schedule;
import com.ware.spring.schedule.domain.ScheduleDto;
import com.ware.spring.schedule.repository.ScheduleRepository;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    // 일정 생성
    @Transactional
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        Schedule schedule = scheduleDto.toEntity();
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return ScheduleDto.toDto(savedSchedule);
    }

    // 일정 수정
    @Transactional
    public void updateSchedule(Long id, ScheduleDto scheduleDto) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
        schedule.update(scheduleDto);  // Schedule 엔티티의 update 메서드 호출
        scheduleRepository.save(schedule);
    }

    // 일정 삭제
    @Transactional
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    // 특정 사용자의 일정 목록 조회
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesForUser(String username) {
        return scheduleRepository.findByMemberMemId(username)
                .stream()
                .map(ScheduleDto::toDto)
                .collect(Collectors.toList());
    }
}
