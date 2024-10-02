package com.ware.spring.workingTime.controller;

import com.ware.spring.workingTime.service.WorkingTimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/workingTime")
public class WorkingTimeApiController {

    private final WorkingTimeService workingTimeService;

    public WorkingTimeApiController(WorkingTimeService workingTimeService) {
        this.workingTimeService = workingTimeService;
    }

    // 출근 시 호출
    @PostMapping("/start")
    public ResponseEntity<String> startWork(@RequestParam int memNo) {
        workingTimeService.startWork(memNo);
        return ResponseEntity.ok("출근이 시작되었습니다.");
    }

    // 퇴근 시 호출
    @PostMapping("/end")
    public ResponseEntity<String> endWork(@RequestParam int memNo) {
        LocalDateTime endTime = LocalDateTime.now();
        workingTimeService.endWork(memNo, endTime);
        return ResponseEntity.ok("퇴근이 완료되었습니다.");
    }
}
