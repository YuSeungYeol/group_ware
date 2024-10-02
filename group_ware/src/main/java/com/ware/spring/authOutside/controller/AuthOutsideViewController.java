package com.ware.spring.authOutside.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ware.spring.authOutside.domain.AuthOutsideDto;
import com.ware.spring.authOutside.service.AuthOutsideService;

@Controller
public class AuthOutsideViewController {

    private final AuthOutsideService authOutsideService;

    @Autowired
    public AuthOutsideViewController(AuthOutsideService authOutsideService) {
        this.authOutsideService = authOutsideService;
    }

    // 목록 페이지로 이동
    @GetMapping("/authOutsideList")
    public String selectAuthOutsideList(Model model) {
        List<AuthOutsideDto> resultList = authOutsideService.selectAuthOutsideList();
        model.addAttribute("resultList", resultList);
        return "authorization/authorizationOutsideList"; // 템플릿 경로에 맞게 수정
    }

    // 상세 페이지로 이동
    @GetMapping("/authOutside")
    public String selectAuthOutside() {
        return "authOutside/authOutsideView"; // 템플릿 경로에 맞게 수정
    }

    @GetMapping("/authOutside/authOutsideCreate")
    public String createAuthOutsidePage() {
        return "authorization/authorizationOutside"; // 템플릿 경로에 맞게 수정
    }

    // 모달 페이지 이동
    @GetMapping("/authOutside/authOutsidemodal")
    public String showAuthOutsideModal() {
        return "authOutside/authOutsideModal"; // 템플릿 경로에 맞게 수정
    }
}
