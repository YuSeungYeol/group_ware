package com.ware.spring.schedule.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ware.spring.schedule.service.ScheduleService;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping(value = "/sse", produces = "text/event-stream")
    public SseEmitter getSseEmitter() {
        SseEmitter emitter = new SseEmitter(0L); // 무한 시간 타임아웃 설정
        scheduleService.addEmitter(emitter);
        return emitter;
    }
}
