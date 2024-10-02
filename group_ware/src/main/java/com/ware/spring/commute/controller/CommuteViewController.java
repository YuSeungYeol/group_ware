package com.ware.spring.commute.controller;

import com.ware.spring.member.domain.Member;
import com.ware.spring.security.vo.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommuteViewController {

    // 로그인한 사용자 정보를 불러와 출근 페이지로 전달
    @GetMapping("/commute")
    public String commutePage(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        Member member = securityUser.getMember();
        model.addAttribute("member", member);
        return "commute/commute";  // commute.html로 이동
    }

    // 출퇴근 기록 페이지로 이동
    @GetMapping("/commute/history")
    public String commuteHistoryPage(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        Member member = securityUser.getMember();
        model.addAttribute("member", member);
        return "commute/history";  // history.html로 이동
    }
}
