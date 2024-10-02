package com.ware.spring.authOvertime.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ware.spring.authOvertime.domain.AuthOvertimeDto;
import com.ware.spring.authOvertime.service.AuthOvertimeService;

@Controller
public class AuthOvertimeViewController {

    private final AuthOvertimeService authOvertimeService;

    @Autowired
    public AuthOvertimeViewController(AuthOvertimeService authOvertimeService) {
        this.authOvertimeService = authOvertimeService;
    }

    // 목록 페이지로 이동
    @GetMapping("/authOvertimeList")
    public String selectAuthOvertimeList(Model model) {
        List<AuthOvertimeDto> resultList = authOvertimeService.selectAuthOvertimeList();
        model.addAttribute("resultList", resultList);
        return "authorization/authorizationOvertimeList"; // 템플릿 경로에 맞게 수정
    }

    // 상세 페이지로 이동
    @GetMapping("/authOvertime")
    public String selectAuthOvertime() {
        return "authOvertime/authOvertimeView"; // 템플릿 경로에 맞게 수정
    }

    @GetMapping("/authOvertime/authOvertimeCreate")
    public String createAuthOvertimePage() {
        return "authorization/authorizationOvertime"; // 템플릿 경로에 맞게 수정
    }

    // 모달 페이지 이동
    @GetMapping("/authOvertime/authOvertimemodal")
    public String showAuthOvertimeModal() {
        return "authOvertime/authOvertimeModal"; // 템플릿 경로에 맞게 수정
    }
}
