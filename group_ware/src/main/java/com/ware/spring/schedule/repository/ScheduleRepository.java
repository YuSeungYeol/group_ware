package com.ware.spring.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ware.spring.schedule.domain.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    List<Schedule> findByMemberMemId(String memId);

}
