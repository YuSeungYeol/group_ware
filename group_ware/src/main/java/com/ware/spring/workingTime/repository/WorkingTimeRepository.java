package com.ware.spring.workingTime.repository;

import com.ware.spring.workingTime.domain.WorkingTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkingTimeRepository extends JpaRepository<WorkingTime, Integer> {
    WorkingTime findByMemNo(int memNo);  // 멤버 번호로 검색
}
