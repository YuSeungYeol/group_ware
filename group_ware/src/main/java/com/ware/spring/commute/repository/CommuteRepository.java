package com.ware.spring.commute.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ware.spring.commute.domain.Commute;
import com.ware.spring.member.domain.Member;

public interface CommuteRepository extends JpaRepository<Commute, Long> {

    // 오늘 해당 회원의 출근 기록을 찾는 메서드
    Optional<Commute> findTodayCommuteByMemberAndCommuteOnStartTimeBetween(Member member, LocalDateTime startOfDay, LocalDateTime endOfDay);

    // 오늘 해당 회원의 출근 기록을 찾는 기본 메서드
    default Optional<Commute> findTodayCommuteByMember(Member member) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        return findTodayCommuteByMemberAndCommuteOnStartTimeBetween(member, startOfDay, endOfDay);
    }
}
