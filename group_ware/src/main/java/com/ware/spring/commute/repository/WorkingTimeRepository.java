package com.ware.spring.commute.repository;

import com.ware.spring.commute.domain.WorkingTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkingTimeRepository extends JpaRepository<WorkingTime, Long> {
    Optional<WorkingTime> findByMemNo(Long memNo);
}
