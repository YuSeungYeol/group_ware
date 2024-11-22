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

    public CommuteService(WorkingTimeRepository workingTimeRepository,CommuteRepository commuteRepository, MemberRepository memberRepository, WeeklyWorkingTimeRepository weeklyWorkingTimeRepository) {
        this.commuteRepository = commuteRepository;
        this.memberRepository = memberRepository;
        this.weeklyWorkingTimeRepository = weeklyWorkingTimeRepository;
        this.workingTimeRepository = workingTimeRepository;
    }

    // 오늘 출근 기록 여부 확인
    public boolean hasTodayCommute(Long memNo) {
        Member member = memberRepository.findById(memNo)
            .orElse(null);  // null 처리
        if (member == null) {
            // 로그로 오류 기록
            System.out.println("존재하지 않는 회원입니다: " + memNo);
            return false;
        }

        Optional<Commute> todayCommute = commuteRepository.findTodayCommuteByMember(member);
        return todayCommute.isPresent();
    }

    // 출근 기록
    public Commute startWork(Long memNo) {
        // memNo로 Member 객체 조회
        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 오늘 해당 회원의 출근 기록이 있는지 확인
        Optional<Commute> existingCommute = commuteRepository.findTodayCommuteByMember(member);

        if (existingCommute.isPresent()) {
            return existingCommute.get();  // 이미 출근 기록이 있으면 해당 기록을 반환
        } else {
            // 현재 시간 (한국 표준시)
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            // 주말인지 확인
            boolean isWeekend = now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY;

            // 지각 여부 설정: 평일 오전 9시 이후 출근 시 지각으로 처리
            String isLate = isWeekend ? "N" : (now.getHour() >= 9 ? "Y" : "N");

            // 새로운 출근 기록 생성
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

    // 퇴근 기록 및 근무 시간 계산
    public Map<String, Object> endWork(Long memNo) {
        try {
            System.out.println("endWork 시작: memNo = " + memNo);

            // memNo로 Member 객체 조회
            Member member = memberRepository.findById(memNo)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memNo: " + memNo));

            Optional<Commute> commuteOpt = commuteRepository.findTodayCommuteByMember(member);
            if (commuteOpt.isPresent()) {
                Commute commute = commuteOpt.get();
                LocalDateTime endTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
                commute.setCommuteOnEndTime(endTime);
                commute.setCommuteFlagBlue("N");
                commute.setCommuteFlagPurple("N");

                // 출근 시간과 퇴근 시간의 차이를 계산
                LocalDateTime startTime = commute.getCommuteOnStartTime();
                if (startTime == null) {
                    throw new IllegalStateException("출근 시간이 null입니다. memNo: " + memNo);
                }
                
                Duration duration = Duration.between(startTime, endTime);
                long hoursWorked = duration.toHours();
                long minutesWorked = duration.toMinutes() % 60;

                // commute_out_time 계산 및 설정
                commute.setCommuteOutTime(java.sql.Time.valueOf(String.format("%02d:%02d:00", hoursWorked, minutesWorked)));
                
                // DB 업데이트
                commuteRepository.save(commute);

                // 주간 및 총 근무 시간 업데이트
                updateWeeklyWorkingTime(memNo);
                updateTotalWorkingTime(memNo);

                // 결과를 Map으로 반환
                Map<String, Object> result = new HashMap<>();
                result.put("hoursWorked", hoursWorked);
                result.put("minutesWorked", minutesWorked);
                result.put("startTime", startTime);
                result.put("endTime", endTime);

                System.out.println("endWork 완료: memNo = " + memNo);
                return result;
            } else {
                throw new IllegalStateException("출근 기록이 존재하지 않습니다. memNo: " + memNo);
            }
        } catch (Exception e) {
            System.err.println("endWork 오류 발생: memNo = " + memNo);
            e.printStackTrace(); // 자세한 오류 로그 출력
            throw e; // 오류를 다시 던져서 컨트롤러에서 처리할 수 있게 함
        }
    }


    // 주간 근무 시간 업데이트 메서드
    private void updateWeeklyWorkingTime(Long memNo) {
        // 한국 시간 기준으로 오늘 날짜 가져오기
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = today.with(DayOfWeek.SUNDAY).atTime(23, 59, 59);

        // 주간 근무 시간(초 단위) 조회
        long totalSeconds = commuteRepository.findTotalCommuteOutTimeForWeek(memNo, startOfWeek, endOfWeek);

        int totalHours = (int) (totalSeconds / 3600);
        int totalMinutes = (int) ((totalSeconds % 3600) / 60);

        // 해당 주차에 대해 memNo가 동일한 데이터 조회
        Optional<WeeklyWorkingTime> weeklyWorkingTimeOpt = weeklyWorkingTimeRepository.findByMemNoAndStartOfWeek(memNo, startOfWeek.toLocalDate());
        WeeklyWorkingTime weeklyWorkingTime;

        if (weeklyWorkingTimeOpt.isPresent()) {
            // 기존 주간 근무 시간이 있으면 업데이트
            weeklyWorkingTime = weeklyWorkingTimeOpt.get();
            weeklyWorkingTime.setWeekHours(totalHours);
            weeklyWorkingTime.setWeekMinutes(totalMinutes);
        } else {
            // 없으면 새로 생성하여 주간 근무 시간 추가
            weeklyWorkingTime = WeeklyWorkingTime.builder()
                    .memNo(memNo)
                    .weekHours(totalHours)
                    .weekMinutes(totalMinutes)
                    .startOfWeek(startOfWeek.toLocalDate())
                    .endOfWeek(endOfWeek.toLocalDate())
                    .build();
        }

        // 변경된 주간 근무 시간 저장
        weeklyWorkingTimeRepository.save(weeklyWorkingTime);
    }



    // 상태 업데이트 메서드 (착석, 외출, 외근, 식사)
    public void updateStatus(Long memNo, String status) {
        // memNo로 Member 객체 조회
        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Optional<Commute> commuteOpt = commuteRepository.findTodayCommuteByMember(member);
        if (commuteOpt.isPresent()) {
            Commute commute = commuteOpt.get();
            if ("seated".equals(status)) {
                commute.setCommuteFlagBlue("Y");
                commute.setCommuteFlagPurple("N");
            } else {
                commute.setCommuteFlagBlue("N");
                commute.setCommuteFlagPurple("Y");
            }
            commuteRepository.save(commute);
        } else {
            throw new IllegalStateException("출근 기록이 존재하지 않습니다.");
        }
    }

    // 퇴근 시 플래그 상태 업데이트
    public void updateEndStatus(Long memNo) {
        // memNo로 Member 객체 조회
        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Optional<Commute> commuteOpt = commuteRepository.findTodayCommuteByMember(member);
        if (commuteOpt.isPresent()) {
            Commute commute = commuteOpt.get();
            commute.setCommuteFlagBlue("N");
            commute.setCommuteFlagPurple("N");
            commuteRepository.save(commute);
        }
    }
    public String getFormattedWeeklyWorkingTime(Long memNo) {
        LocalDate startOfWeek = LocalDate.now(ZoneId.of("Asia/Seoul")).with(DayOfWeek.MONDAY);
        Optional<WeeklyWorkingTime> weeklyWorkingTimeOpt = weeklyWorkingTimeRepository.findByMemNoAndStartOfWeek(memNo, startOfWeek);

        if (weeklyWorkingTimeOpt.isPresent()) {
            WeeklyWorkingTime weeklyWorkingTime = weeklyWorkingTimeOpt.get();
            return String.format("%d시간 %d분", weeklyWorkingTime.getWeekHours(), weeklyWorkingTime.getWeekMinutes());
        } else {
            return "0시간 0분";
        }
    }
    public void updateTotalWorkingTime(Long memNo) {
        // 특정 멤버의 주간 근무 시간을 전부 조회
        List<WeeklyWorkingTime> weeklyWorkingTimes = weeklyWorkingTimeRepository.findByMemNo(memNo);
        
        int totalHours = 0;
        int totalMinutes = 0;

        for (WeeklyWorkingTime weeklyWorkingTime : weeklyWorkingTimes) {
            // 주간 근무 시간 누적
            totalHours += weeklyWorkingTime.getWeekHours();
            totalMinutes += weeklyWorkingTime.getWeekMinutes();
        }

        // 총 근무 시간 계산 (60분을 1시간으로 변환)
        totalHours += totalMinutes / 60;
        totalMinutes = totalMinutes % 60;

        // 총 근무 일수 계산
        List<Date> distinctWorkingDays = commuteRepository.findDistinctWorkingDays(memNo);
        int totalDays = distinctWorkingDays.size(); // 중복되지 않는 고유한 근무 일수

        // working_time 테이블 업데이트
        Optional<WorkingTime> workingTimeOpt = workingTimeRepository.findByMemNo(memNo);
        WorkingTime workingTime;

        if (workingTimeOpt.isPresent()) {
            // 기존 데이터가 있는 경우 업데이트
            workingTime = workingTimeOpt.get();
            workingTime.setTotalHours(totalHours);
            workingTime.setTotalMinutes(totalMinutes);
            workingTime.setTotalDate(totalDays);
            workingTime.setLastUpdated(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        } else {
            // 기존 데이터가 없는 경우 새로 생성
            workingTime = WorkingTime.builder()
                    .memNo(memNo)
                    .totalHours(totalHours)
                    .totalMinutes(totalMinutes)
                    .totalDate(totalDays)
                    .lastUpdated(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                    .build();
        }

        // 데이터 저장
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