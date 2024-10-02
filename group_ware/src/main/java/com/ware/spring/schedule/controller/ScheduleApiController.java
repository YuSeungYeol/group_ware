package com.ware.spring.schedule.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.schedule.domain.ScheduleDto;
import com.ware.spring.schedule.service.ScheduleService;

@Controller
public class ScheduleApiController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/calendar/schedule/createScheduleWithJson")
    @ResponseBody
    public Map<String, String> createScheduleWithJson(@RequestBody ScheduleDto scheduleDto) {
        Map<String, String> resultMap = new HashMap<>();
        try {
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

            // scheduleDto에 로그인된 사용자의 Member 설정
            scheduleDto.setMember(loggedInMember);

            // 일정 생성
            ScheduleDto createdSchedule = scheduleService.createSchedule(scheduleDto);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "일정이 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "일정 등록 중 오류가 발생했습니다.");
        }
        return resultMap;
    }
    // 로그인된 사용자의 일정 목록 반환
    @GetMapping("/calendar/schedule/getScheduleListForLoggedInUser")
    @ResponseBody
    public List<ScheduleDto> getScheduleListForLoggedInUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return scheduleService.getSchedulesForUser(username);
    }

    // 일정 수정
    @PutMapping("/calendar/schedule/update/{id}")
    @ResponseBody
    public Map<String, String> updateSchedule(
            @PathVariable("id") Long id,
            @RequestBody ScheduleDto scheduleDto) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            scheduleService.updateSchedule(id, scheduleDto);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "일정이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "일정 수정 중 오류가 발생했습니다.");
        }
        return resultMap;
    }

    // 일정 삭제
    @DeleteMapping("/calendar/schedule/delete/{id}")
    @ResponseBody
    public Map<String, String> deleteSchedule(@PathVariable("id") Long id) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            scheduleService.deleteSchedule(id);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "일정이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "일정 삭제 중 오류가 발생했습니다.");
        }
        return resultMap;
    }
    
}
