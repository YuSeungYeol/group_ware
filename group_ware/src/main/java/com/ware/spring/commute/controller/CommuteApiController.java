package com.ware.spring.commute.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ware.spring.commute.domain.Commute;
import com.ware.spring.commute.service.CommuteService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.security.vo.SecurityUser;

@RestController
@RequestMapping("/api/commute")
public class CommuteApiController {

    private final CommuteService commuteService;

    public CommuteApiController(CommuteService commuteService) {
        this.commuteService = commuteService;
    }

    /**
     * 오늘 출근 기록 여부를 확인
     * 
     * @param memNo 회원 번호
     * @return 출근 기록이 있는 경우 true, 그렇지 않은 경우 false
     */
    @GetMapping("/checkTodayCommute")
    public ResponseEntity<Boolean> checkTodayCommute(@RequestParam("memNo") Long memNo) {
        try {
            boolean hasCommute = commuteService.hasTodayCommute(memNo);
            return ResponseEntity.ok(hasCommute);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    /**
     * 현재 로그인된 사용자의 회원 번호를 반환.
     * 
     * @return 회원 번호 (memNo)* 
     **/
    @GetMapping("/getMemNo")
    public Long getMemNo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            Member member = securityUser.getMember();
            return member.getMemNo();
        } else {
            throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
        }
    }

    // 출근 기록 처리 API
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startWork(@RequestParam("memNo") Long memNo) {
        try {
            Commute commute = commuteService.startWork(memNo);
            commuteService.updateTotalWorkingTime(memNo); // 총 근무 시간 업데이트

            // 결과로 지각 여부와 메시지를 반환
            Map<String, Object> response = Map.of(
                "message", "출근이 완료되었습니다.",
                "isLate", commute.getIsLate() // 지각 여부 (Y/N)
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "출근 처리 중 오류가 발생했습니다.",
                "isLate", "N"
            ));
        }
    }

    // 퇴근 기록 및 근무 시간 계산 API
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endWork(@RequestParam("memNo") Long memNo) {
        try {
            Map<String, Object> workTime = commuteService.endWork(memNo);
            commuteService.updateTotalWorkingTime(memNo); // 총 근무 시간 업데이트
            return ResponseEntity.ok(workTime);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 출근 상태 업데이트 API (착석, 외출, 외근, 식사)
    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(@RequestParam("memNo") Long memNo, @RequestParam("status") String status) {
        try {
            commuteService.updateStatus(memNo, status);
            return ResponseEntity.ok(status + " 상태로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상태 업데이트 중 오류가 발생했습니다.");
        }
    }

    // 퇴근 시 플래그 상태 업데이트 API
    @PostMapping("/endStatus")
    public ResponseEntity<String> updateEndStatus(@RequestParam("memNo") Long memNo) {
        try {
            commuteService.updateEndStatus(memNo);
            commuteService.updateTotalWorkingTime(memNo); // 총 근무 시간 업데이트
            return ResponseEntity.ok("퇴근 상태가 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("퇴근 상태 업데이트 중 오류가 발생했습니다.");
        }
    }

    // 오늘 출근 기록 여부 확인 API
    @GetMapping("/hasTodayCommute")
    public ResponseEntity<Boolean> hasTodayCommute(@RequestParam("memNo") Long memNo) {
        try {
            boolean hasCommute = commuteService.hasTodayCommute(memNo);
            return ResponseEntity.ok(hasCommute);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    // 주간 근무 시간 조회 API
    @GetMapping("/weeklyWorkingTime")
    public ResponseEntity<Map<String, Object>> getWeeklyWorkingTime(@RequestParam("memNo") Long memNo) {
        String weeklyWorkingTime = commuteService.getFormattedWeeklyWorkingTime(memNo);
        Map<String, Object> response = new HashMap<>();
        response.put("weeklyWorkingTime", weeklyWorkingTime);
        return ResponseEntity.ok(response);
    }

    // 총 근무 시간 조회 API
    @GetMapping("/totalWorkingTime")
    public ResponseEntity<Map<String, Object>> getTotalWorkingTime(@RequestParam("memNo") Long memNo) {
        try {
            Map<String, Object> totalWorkingTime = commuteService.getTotalWorkingTime(memNo);
            return ResponseEntity.ok(totalWorkingTime);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 연도별 근태 현황 데이터를 가져오는 API
    @GetMapping("/status_single/data")
    public ResponseEntity<Map<String, Object>> getYearlyCommuteData(@RequestParam("year") int year, Principal principal) {
        try {
            Long memNo = commuteService.getMemberNoByUsername(principal.getName());

            Map<Integer, Map<String, Integer>> monthlyWorkingTime = commuteService.getMonthlyWorkingTime(memNo, year);
            int totalWorkingTime = commuteService.getTotalWorkingTime(memNo, year);
            int totalLateCount = commuteService.getTotalLateCount(memNo, year);
            Map<String, Integer> monthlyLateCount = commuteService.getMonthlyLateCount(memNo, year);

            Map<String, Object> response = new HashMap<>();
            response.put("monthlyWorkingTime", monthlyWorkingTime);
            response.put("monthlyLateCount", monthlyLateCount);
            response.put("totalWorkingTime", totalWorkingTime);
            response.put("totalLateCount", totalLateCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "연도별 근태 현황 데이터를 가져오는 중 오류가 발생했습니다.",
                "error", e.getMessage()
            ));
        }
    }
    // 특정 memNo에 대한 주간 근무 시간 및 지각 횟수 조회 API
    @GetMapping("/weekly/{memNo}/{weekOffset}")
    public ResponseEntity<?> getWeeklyWorkingTime(
            @PathVariable("memNo") Long memNo,
            @PathVariable("weekOffset") int weekOffset) {
        try {
            List<Map<String, Object>> weeklyWorkingTime = commuteService.getWeeklyWorkingTimeSummary(memNo, weekOffset);
            return ResponseEntity.ok(weeklyWorkingTime);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "주간 근무 시간 데이터를 가져오는 중 오류가 발생했습니다."));
        }
    }


    // 특정 memNo에 대한 연간 근무 시간 및 지각 횟수 조회 API
    @GetMapping("/annual/{memNo}/{yearOffset}")
    public ResponseEntity<Map<String, Object>> getAnnualWorkingTime(
            @PathVariable("memNo") Long memNo,
            @PathVariable("yearOffset") int yearOffset) {
        try {
            Map<String, Object> annualWorkingTime = commuteService.getAnnualWorkingTimeSummary(memNo, yearOffset);
            return ResponseEntity.ok(annualWorkingTime);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "연간 근무 시간 데이터를 가져오는 중 오류가 발생했습니다."));
        }
    }
}
