package com.ware.spring.commute.repository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 특정 주차에 해당하는 근무 시간 합계 (초 단위)
    @Query("SELECT COALESCE(SUM(TIME_TO_SEC(c.commuteOutTime)), 0) FROM Commute c " +
           "WHERE c.member.memNo = :memNo " +
           "AND c.commuteOnStartTime BETWEEN :startOfWeek AND :endOfWeek")
    long findTotalCommuteOutTimeForWeek(@Param("memNo") Long memNo,
                                        @Param("startOfWeek") LocalDateTime startOfWeek,
                                        @Param("endOfWeek") LocalDateTime endOfWeek);

    // 특정 멤버의 출근 날짜 리스트 조회 (중복 제거)
    @Query("SELECT DISTINCT DATE(commuteOnStartTime) FROM Commute c WHERE c.member.memNo = :memNo")
    List<Date> findDistinctWorkingDays(@Param("memNo") Long memNo);

    // 특정 멤버의 특정 기간 동안의 지각 횟수 조회
    @Query("SELECT c FROM Commute c WHERE c.member.memNo = :memNo AND c.commuteOnStartTime BETWEEN :startDate AND :endDate AND c.isLate = 'Y'")
    List<Commute> findLateCommutesByMemberAndDateRange(@Param("memNo") Long memNo,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    // 특정 연도에 해당하는 모든 월별 지각 기록 조회
    @Query("SELECT c FROM Commute c WHERE c.member.memNo = :memNo " +
           "AND YEAR(c.commuteOnStartTime) = :year " +
           "AND c.isLate = 'Y'")
    List<Commute> findLateCommutesByYear(@Param("memNo") Long memNo, @Param("year") int year);

    // 특정 연도에 해당하는 모든 월별 근무 시간 조회
    @Query("SELECT c FROM Commute c WHERE c.member.memNo = :memNo " +
           "AND YEAR(c.commuteOnStartTime) = :year")
    List<Commute> findCommutesByYear(@Param("memNo") Long memNo, @Param("year") int year);
    @Query("SELECT c FROM Commute c WHERE c.member.memNo = :memNo " +
    	       "AND c.commuteOnStartTime BETWEEN :startDate AND :endDate")
    	List<Commute> findCommutesByYearAndMonth(@Param("memNo") Long memNo,
    	                                         @Param("startDate") LocalDateTime startDate,
    	                                         @Param("endDate") LocalDateTime endDate);

    	// 특정 연도에 해당하는 모든 근무 시간 조회
    	@Query("SELECT c FROM Commute c WHERE c.member.memNo = :memNo " +
    	       "AND c.commuteOnStartTime BETWEEN :startDate AND :endDate")
    	List<Commute> findCommutesByYear(@Param("memNo") Long memNo,
    	                                 @Param("startDate") LocalDateTime startDate,
    	                                 @Param("endDate") LocalDateTime endDate);

    	// 특정 연도에 해당하는 모든 지각 기록 조회
    	@Query("SELECT c FROM Commute c WHERE c.member.memNo = :memNo " +
    	       "AND c.commuteOnStartTime BETWEEN :startDate AND :endDate " +
    	       "AND c.isLate = 'Y'")
    	List<Commute> findLateCommutesByYear(@Param("memNo") Long memNo,
    	                                     @Param("startDate") LocalDateTime startDate,
    	                                     @Param("endDate") LocalDateTime endDate);
    	@Query("SELECT c FROM Commute c WHERE c.member.memNo = :memNo AND c.commuteOnStartTime BETWEEN :startDateTime AND :endDateTime")
    	    List<Commute> findCommutesByMemberAndDateRange(@Param("memNo") Long memNo, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

}
