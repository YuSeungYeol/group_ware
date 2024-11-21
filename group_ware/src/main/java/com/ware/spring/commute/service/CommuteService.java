package com.ware.spring.commute.service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ware.spring.commute.domain.Commute;
import com.ware.spring.commute.domain.WeeklyWorkingTime;
import com.ware.spring.commute.domain.WorkingTime;
import com.ware.spring.commute.repository.CommuteRepository;
import com.ware.spring.commute.repository.WeeklyWorkingTimeRepository;
import com.ware.spring.commute.repository.WorkingTimeRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
@Service
public class CommuteService {

    private final CommuteRepository commuteRepository;
    private final MemberRepository memberRepository;
    private final WeeklyWorkingTimeRepository weeklyWorkingTimeRepository;
    private final WorkingTimeRepository workingTimeRepository;

    public CommuteService(WorkingTimeRepository workingTimeRepository, CommuteRepository commuteRepository, 
                          MemberRepository memberRepository, WeeklyWorkingTimeRepository weeklyWorkingTimeRepository) {
        this.commuteRepository = commuteRepository;
        this.memberRepository = memberRepository;
        this.weeklyWorkingTimeRepository = weeklyWorkingTimeRepository;
        this.workingTimeRepository = workingTimeRepository;
    }

    /**
     * 오늘의 출근 기록 여부를 확인합니다.
     * 
     * @param memNo 회원 번호
     * @return 오늘의 출근 기록이 있는 경우 true, 그렇지 않은 경우 false
     */
    public boolean hasTodayCommute(Long memNo) {
        Member member = memberRepository.findById(memNo).orElse(null);
        if (member == null) {
            System.out.println("존재하지 않는 회원입니다: " + memNo);
            return false;
        }
        Optional<Commute> todayCommute = commuteRepository.findTodayCommuteByMember(member);
        return todayCommute.isPresent();
    }

    /**
     * 출근 기록을 생성합니다.
     * 
     * @param memNo 회원 번호
     * @return 생성된 출근 기록
     */
    public Commute startWork(Long memNo) {
        Member member = memberRepository.findById(memNo)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Optional<Commute> existingCommute = commuteRepository.findTodayCommuteByMember(member);
        if (existingCommute.isPresent()) {
            return existingCommute.get();  // 기존 기록 반환
        } else {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            String isLate = (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) ? 
                            "N" : (now.getHour() >= 9 ? "Y" : "N");

            Commute commute = Commute.builder()
                    .member(member)
                    .commuteOnStartTime(now)
                    .commuteFlagBlue("Y")
                    .commuteFlagPurple("N")
                    .isLate(isLate)
                    .build();

            return commuteRepository.save(commute);
        }
    }

    /**
     * 퇴근 기록을 생성하고 근무 시간을 계산합니다.
     * 
     * @param memNo 회원 번호
     * @return 근무 시간 및 기타 정보
     */
    public Map<String, Object> endWork(Long memNo) {
        try {
            Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            Optional<Commute> commuteOpt = commuteRepository.findTodayCommuteByMember(member);
            if (commuteOpt.isPresent()) {
                Commute commute = commuteOpt.get();
                LocalDateTime endTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
                commute.setCommuteOnEndTime(endTime);
                commute.setCommuteFlagBlue("N");
                commute.setCommuteFlagPurple("N");

                LocalDateTime startTime = commute.getCommuteOnStartTime();
                if (startTime == null) {
                    throw new IllegalStateException("출근 시간이 null입니다.");
                }

                Duration duration = Duration.between(startTime, endTime);
                long hoursWorked = duration.toHours();
                long minutesWorked = duration.toMinutes() % 60;

                commute.setCommuteOutTime(java.sql.Time.valueOf(String.format("%02d:%02d:00", hoursWorked, minutesWorked)));
                commuteRepository.save(commute);

                updateWeeklyWorkingTime(memNo);
                updateTotalWorkingTime(memNo);

                Map<String, Object> result = new HashMap<>();
                result.put("hoursWorked", hoursWorked);
                result.put("minutesWorked", minutesWorked);
                result.put("startTime", startTime);
                result.put("endTime", endTime);

                return result;
            } else {
                throw new IllegalStateException("출근 기록이 존재하지 않습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 주간 근무 시간을 업데이트합니다.
     * 
     * @param memNo 회원 번호
     */
    private void updateWeeklyWorkingTime(Long memNo) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = today.with(DayOfWeek.SUNDAY).atTime(23, 59, 59);

        long totalSeconds = commuteRepository.findTotalCommuteOutTimeForWeek(memNo, startOfWeek, endOfWeek);

        int totalHours = (int) (totalSeconds / 3600);
        int totalMinutes = (int) ((totalSeconds % 3600) / 60);

        Optional<WeeklyWorkingTime> weeklyWorkingTimeOpt = weeklyWorkingTimeRepository.findByMemNoAndStartOfWeek(memNo, startOfWeek.toLocalDate());
        WeeklyWorkingTime weeklyWorkingTime;

        if (weeklyWorkingTimeOpt.isPresent()) {
            weeklyWorkingTime = weeklyWorkingTimeOpt.get();
            weeklyWorkingTime.setWeekHours(totalHours);
            weeklyWorkingTime.setWeekMinutes(totalMinutes);
        } else {
            weeklyWorkingTime = WeeklyWorkingTime.builder()
                    .memNo(memNo)
                    .weekHours(totalHours)
                    .weekMinutes(totalMinutes)
                    .startOfWeek(startOfWeek.toLocalDate())
                    .endOfWeek(endOfWeek.toLocalDate())
                    .build();
        }

        weeklyWorkingTimeRepository.save(weeklyWorkingTime);
    }

    /**
     * 총 근무 시간을 업데이트합니다.
     * 
     * @param memNo 회원 번호
     */
    public void updateTotalWorkingTime(Long memNo) {
        List<WeeklyWorkingTime> weeklyWorkingTimes = weeklyWorkingTimeRepository.findByMemNo(memNo);

        int totalHours = 0;
        int totalMinutes = 0;
        for (WeeklyWorkingTime weeklyWorkingTime : weeklyWorkingTimes) {
            totalHours += weeklyWorkingTime.getWeekHours();
            totalMinutes += weeklyWorkingTime.getWeekMinutes();
        }

        totalHours += totalMinutes / 60;
        totalMinutes = totalMinutes % 60;

        List<Date> distinctWorkingDays = commuteRepository.findDistinctWorkingDays(memNo);
        int totalDays = distinctWorkingDays.size();

        Optional<WorkingTime> workingTimeOpt = workingTimeRepository.findByMemNo(memNo);
        WorkingTime workingTime;

        if (workingTimeOpt.isPresent()) {
            workingTime = workingTimeOpt.get();
            workingTime.setTotalHours(totalHours);
            workingTime.setTotalMinutes(totalMinutes);
            workingTime.setTotalDate(totalDays);
            workingTime.setLastUpdated(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        } else {
            workingTime = WorkingTime.builder()
                    .memNo(memNo)
                    .totalHours(totalHours)
                    .totalMinutes(totalMinutes)
                    .totalDate(totalDays)
                    .lastUpdated(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                    .build();
        }

        workingTimeRepository.save(workingTime);
    }
    public Map<String, Object> getTotalWorkingTime(Long memNo) {
        Optional<WorkingTime> workingTimeOpt = workingTimeRepository.findByMemNo(memNo);
        
        if (workingTimeOpt.isPresent()) {
            WorkingTime workingTime = workingTimeOpt.get();
            Map<String, Object> result = new HashMap<>();
            result.put("totalHours", workingTime.getTotalHours());
            result.put("totalMinutes", workingTime.getTotalMinutes());
            result.put("totalDays", workingTime.getTotalDate());
            result.put("lastUpdated", workingTime.getLastUpdated());
            return result;
        } else {
            throw new IllegalArgumentException("총 근무 시간 기록이 존재하지 않습니다.");
        }
    }
    // 사용자 memNo를 memId로 가져오기
    public Long getMemberNoByUsername(String username) {
        return memberRepository.findByMemId(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."))
                .getMemNo();
    }

    


    public int getTotalWorkingTime(Long memNo, int year) {
        YearMonth startOfYear = YearMonth.of(year, 1);
        YearMonth endOfYear = YearMonth.of(year, 12);

        LocalDateTime startDate = startOfYear.atDay(1).atStartOfDay();
        LocalDateTime endDate = endOfYear.atEndOfMonth().atTime(23, 59, 59);

        List<Commute> commutes = commuteRepository.findCommutesByYear(memNo, startDate, endDate);
        return commutes.stream()
                .mapToInt(commute -> commute.getCommuteOutTime().toLocalTime().getHour())
                .sum();
    }

    public int getTotalLateCount(Long memNo, int year) {
        YearMonth startOfYear = YearMonth.of(year, 1);
        YearMonth endOfYear = YearMonth.of(year, 12);

        LocalDateTime startDate = startOfYear.atDay(1).atStartOfDay();
        LocalDateTime endDate = endOfYear.atEndOfMonth().atTime(23, 59, 59);

        List<Commute> lateCommutes = commuteRepository.findLateCommutesByYear(memNo, startDate, endDate);
        return lateCommutes.size();
    }
 // 월별 지각 횟수를 계산하는 메서드
    public Map<String, Integer> getMonthlyLateCount(Long memNo, int year) {
        Map<String, Integer> monthlyLateCount = new HashMap<>();

        // 각 월별로 데이터를 초기화 (1월 ~ 12월)
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay(); // 해당 월의 시작 일자 (자정)
            LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59); // 해당 월의 종료 일자 (23:59:59)

            // 해당 월의 지각 횟수를 조회
            List<Commute> lateCommutes = commuteRepository.findLateCommutesByMemberAndDateRange(memNo, startDate, endDate);
            monthlyLateCount.put(String.valueOf(month), lateCommutes.size());
        }

        return monthlyLateCount;
    }

    public Map<Integer, Map<String, Integer>> getMonthlyWorkingTime(Long memNo, int year) {
        Map<Integer, Map<String, Integer>> monthlyWorkingTime = new HashMap<>();

        // 예를 들어 DB에서 주간 데이터를 가져오는 로직을 작성합니다.
        List<WeeklyWorkingTime> weeklyWorkingTimes = weeklyWorkingTimeRepository.findByMemNoAndYear(memNo, year);

        for (WeeklyWorkingTime weekly : weeklyWorkingTimes) {
            // 주별 데이터에서 해당 주의 월을 가져옵니다.
            int month = weekly.getStartOfWeek().getMonthValue(); // 월 값을 가져옴 (1~12)

            // 월별 합산을 위해 초기 값 설정
            monthlyWorkingTime.putIfAbsent(month, new HashMap<>(Map.of("hours", 0, "minutes", 0)));

            // 현재 월의 기존 시간 및 분
            int currentHours = monthlyWorkingTime.get(month).get("hours");
            int currentMinutes = monthlyWorkingTime.get(month).get("minutes");

            // 주간 데이터를 월별로 합산
            int updatedHours = currentHours + weekly.getWeekHours();
            int updatedMinutes = currentMinutes + weekly.getWeekMinutes();

            // 분을 시간으로 변환 (60분 초과 시)
            updatedHours += updatedMinutes / 60;
            updatedMinutes = updatedMinutes % 60;

            monthlyWorkingTime.get(month).put("hours", updatedHours);
            monthlyWorkingTime.get(month).put("minutes", updatedMinutes);
        }

        return monthlyWorkingTime;
    }
    public List<Commute> getWeeklyCommuteStatus(Long memNo, LocalDate startDate, LocalDate endDate) {
        // startDate부터 endDate까지의 출퇴근 기록을 조회합니다.
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 특정 멤버의 해당 주간의 근태 기록을 가져옵니다.
        List<Commute> weeklyCommuteList = commuteRepository.findCommutesByMemberAndDateRange(memNo, startDateTime, endDateTime);
        
        return weeklyCommuteList;
    }

 

    public List<Map<String, Object>> getWeeklyWorkingTimeSummary(Long memNo, int weekOffset) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul")).plusWeeks(weekOffset);
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        // 주간 데이터 초기화 (월요일부터 일요일까지)
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
            Map<String, Object> commuteData = new HashMap<>();
            String formattedDate = String.format("%d월 %d일", date.getMonthValue(), date.getDayOfMonth());
            commuteData.put("date", formattedDate);
            commuteData.put("hours", 0);
            commuteData.put("minutes", 0);
            commuteData.put("isLate", "N");
            responseList.add(commuteData);
        }

        // 해당 주간의 실제 근무 데이터 가져오기
        List<Commute> weeklyCommutes = commuteRepository.findCommutesByMemberAndDateRange(memNo, startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59));

        // 실제 근무 데이터를 주간 데이터에 반영
        for (Commute commute : weeklyCommutes) {
            if (commute.getCommuteOnStartTime() != null && commute.getCommuteOnEndTime() != null) {
                LocalDateTime startTime = commute.getCommuteOnStartTime();
                int dayIndex = startTime.getDayOfWeek().getValue() - 1; // 월요일이 0번째 인덱스

                Map<String, Object> commuteData = responseList.get(dayIndex);
                Duration duration = Duration.between(commute.getCommuteOnStartTime(), commute.getCommuteOnEndTime());
                int hours = duration.toHoursPart();
                int minutes = duration.toMinutesPart();

                commuteData.put("hours", (int) commuteData.get("hours") + hours);
                commuteData.put("minutes", (int) commuteData.get("minutes") + minutes);
                if ("Y".equals(commute.getIsLate())) {
                    commuteData.put("isLate", "Y");
                }
            }
        }

        return responseList;
    }





    public Map<String, Object> getAnnualWorkingTimeSummary(Long memNo, int yearOffset) {
        int year = LocalDate.now().getYear() + yearOffset;
        YearMonth startOfYear = YearMonth.of(year, 1);
        YearMonth endOfYear = YearMonth.of(year, 12);
        LocalDateTime startDate = startOfYear.atDay(1).atStartOfDay();
        LocalDateTime endDate = endOfYear.atEndOfMonth().atTime(23, 59, 59);

        List<Commute> annualCommutes = commuteRepository.findCommutesByMemberAndDateRange(memNo, startDate, endDate);

        int[] monthlyHours = new int[12];
        int[] monthlyMinutes = new int[12];
        int[] monthlyLateCounts = new int[12];

        for (Commute commute : annualCommutes) {
            if (commute.getCommuteOnStartTime() != null && commute.getCommuteOnEndTime() != null) {
                int monthIndex = commute.getCommuteOnStartTime().getMonthValue() - 1; // 월 인덱스 (0부터 시작)

                Duration duration = Duration.between(commute.getCommuteOnStartTime(), commute.getCommuteOnEndTime());
                monthlyHours[monthIndex] += duration.toHours();
                monthlyMinutes[monthIndex] += duration.toMinutesPart();

                if ("Y".equals(commute.getIsLate())) {
                    monthlyLateCounts[monthIndex]++;
                }
            }
        }

        // 시간과 분 계산 (60분 이상이면 시간으로 전환)
        for (int i = 0; i < 12; i++) {
            monthlyHours[i] += monthlyMinutes[i] / 60;
            monthlyMinutes[i] %= 60;
        }

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("monthlyHours", monthlyHours);
        response.put("monthlyMinutes", monthlyMinutes);
        response.put("monthlyLateCounts", monthlyLateCounts);

        return response;
    }



}