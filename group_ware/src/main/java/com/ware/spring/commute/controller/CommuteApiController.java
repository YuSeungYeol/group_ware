package com.ware.spring.commute.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    // 오늘 출근 기록 확인 API
    @GetMapping("/checkTodayCommute")
    public ResponseEntity<Boolean> checkTodayCommute(@RequestParam("memNo") Long memNo) {
        try {
            boolean hasCommute = commuteService.hasTodayCommute(memNo);
            return ResponseEntity.ok(hasCommute);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    // 현재 로그인된 사용자의 memNo를 반환하는 API
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
    public ResponseEntity<String> startWork(@RequestParam("memNo") Long memNo) {
        try {
            commuteService.startWork(memNo);
            return ResponseEntity.ok("출근이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("출근 처리 중 오류가 발생했습니다.");
        }
    }

    // 퇴근 기록 및 근무 시간 계산 API
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endWork(@RequestParam("memNo") Long memNo) {
        try {
            Map<String, Object> workTime = commuteService.endWork(memNo);
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
}
