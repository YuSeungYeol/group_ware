package com.ware.spring.authOff.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ware.spring.authOff.domain.AuthOffDto;
import com.ware.spring.authOff.service.AuthOffService;

@Controller
public class AuthOffViewController {

    private final AuthOffService authOffService;

    @Autowired
    public AuthOffViewController(AuthOffService authOffService) {
        this.authOffService = authOffService;
    }

    // 목록 페이지로 이동
    @GetMapping("/authOffList")
    public String selectAuthOffList(Model model) {
        List<AuthOffDto> resultList = authOffService.selectAuthOffList();
        model.addAttribute("resultList", resultList);
        return "authorization/authorizationOffList"; // 템플릿 경로에 맞게 수정
    }

    // 상세 페이지로 이동
    @GetMapping("/authOff")
    public String selectAuthOff() {
        return "authOff/authOffView"; // 템플릿 경로에 맞게 수정
    }

    @GetMapping("/authOff/authOffCreate")
    public String createAuthOffPage() {
        return "authorization/authorizationOff"; // 템플릿 경로에 맞게 수정
    }

    // 모달 페이지 이동
    @GetMapping("/authOff/authOffmodal")
    public String showAuthOffModal() {
        return "authOff/authOffModal"; // 템플릿 경로에 맞게 수정
    }
}