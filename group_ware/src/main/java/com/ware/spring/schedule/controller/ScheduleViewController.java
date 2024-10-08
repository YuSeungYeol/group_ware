package com.ware.spring.schedule.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.schedule.domain.ScheduleDto;
import com.ware.spring.schedule.service.ScheduleService;

@Controller
@RequestMapping("/calendar")
public class ScheduleViewController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private MemberRepository memberRepository;

    // /calendar 경로 처리 (로그인된 사용자의 일정)
    @GetMapping
    public String showSchedulePage(Model model) {
        // 현재 로그인된 사용자 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        // 로그인된 사용자의 Member 정보 가져오기
        Member loggedInMember = memberRepository.findByMemId(username)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));

        // 로그인된 사용자의 일정과 공지사항을 가져와서 모델에 추가
        model.addAttribute("events", scheduleService.getAllSchedulesAndNotices(username));
        model.addAttribute("member", loggedInMember);

        return "/calendar/calendar";  // templates/calendar/calendar.html 파일 반환
    }

    @GetMapping("/{memberNo}")
    public String showMemberSchedulePage(@PathVariable("memberNo") Long memberNo, Model model) {
        System.out.println("조회된 멤버 번호: " + memberNo); // 서버 콘솔에 멤버 번호 출력
        List<ScheduleDto> schedules = scheduleService.getSchedulesByMemberNo(memberNo);
        model.addAttribute("schedules", schedules); // 모델에 일정 목록 추가
        return "calendar/member_calendar"; // 멤버별 일정 페이지 반환	
    }

}