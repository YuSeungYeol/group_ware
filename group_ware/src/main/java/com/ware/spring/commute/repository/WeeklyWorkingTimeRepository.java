package com.ware.spring.commute.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ware.spring.commute.domain.WeeklyWorkingTime;

public interface WeeklyWorkingTimeRepository extends JpaRepository<WeeklyWorkingTime, Long> {
    
    List<WeeklyWorkingTime> findByMemNo(Long memNo);

    Optional<WeeklyWorkingTime> findByMemNoAndStartOfWeek(Long memNo, LocalDate startOfWeek);

    @Query("SELECT COALESCE(SUM(TIME_TO_SEC(c.commuteOutTime)), 0) FROM Commute c " +
           "WHERE c.member.memNo = :memNo " +
           "AND c.commuteOnStartTime BETWEEN :startOfWeek AND :endOfWeek")
    Long findTotalWorkingSecondsForWeek(@Param("memNo") Long memNo,
                                        @Param("startOfWeek") LocalDate startOfWeek,
                                        @Param("endOfWeek") LocalDate endOfWeek);
    // 특정 연도의 특정 사용자에 대한 모든 주간 근무 데이터를 가져오는 메서드
    @Query("SELECT w FROM WeeklyWorkingTime w WHERE w.memNo = :memNo AND YEAR(w.startOfWeek) = :year")
    List<WeeklyWorkingTime> findByMemNoAndYear(@Param("memNo") Long memNo, @Param("year") int year);

    // 특정 월에 속한 주간 근무 데이터를 가져오는 메서드 (월별 계산 시 사용 가능)
    @Query("SELECT w FROM WeeklyWorkingTime w WHERE w.memNo = :memNo AND MONTH(w.startOfWeek) = :month AND YEAR(w.startOfWeek) = :year")
    List<WeeklyWorkingTime> findByMemNoAndMonth(@Param("memNo") Long memNo, @Param("month") int month, @Param("year") int year);

    // 특정 직원의 특정 주간 범위 내의 근무 시간 기록을 가져오기 위한 메서드
    @Query("SELECT w FROM WeeklyWorkingTime w WHERE w.memNo = :memNo AND w.startOfWeek >= :startOfWeek AND w.endOfWeek <= :endOfWeek")
    Optional<WeeklyWorkingTime> findWeeklyByMemNoAndWeekRange(@Param("memNo") Long memNo,
                                                              @Param("startOfWeek") LocalDate startOfWeek,
                                                              @Param("endOfWeek") LocalDate endOfWeek);

    // 특정 월 또는 연간 범위의 모든 주간 데이터를 조회하는 메서드
    @Query("SELECT w FROM WeeklyWorkingTime w WHERE w.memNo = :memNo AND w.startOfWeek >= :startOfMonth AND w.endOfWeek <= :endOfMonth")
    List<WeeklyWorkingTime> findMonthlyByMemNoAndMonthRange(@Param("memNo") Long memNo,
                                                            @Param("startOfMonth") LocalDate startOfMonth,
                                                            @Param("endOfMonth") LocalDate endOfMonth);
    @Query("SELECT w FROM WeeklyWorkingTime w WHERE w.memNo = :memNo AND w.startOfWeek >= :startDate AND w.endOfWeek <= :endDate")
    List<WeeklyWorkingTime> findByMemNoAndDateRange(@Param("memNo") Long memNo,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}

