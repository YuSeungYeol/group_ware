package com.ware.spring.workingTime.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/workingTime")
public class WorkingTimeViewController {

    // 근무 시간 페이지 이동
    @GetMapping("/view")
    public String viewWorkingTimePage() {
        return "workingTime/viewWorkingTime";  // viewWorkingTime.html 페이지로 이동
    }
}
