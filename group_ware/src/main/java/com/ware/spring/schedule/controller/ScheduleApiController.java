package com.ware.spring.schedule.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.notice.domain.Notice;
import com.ware.spring.notice.service.NoticeService;
import com.ware.spring.schedule.domain.ScheduleDto;
import com.ware.spring.schedule.service.ScheduleService;

@Controller
public class ScheduleApiController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private NoticeService noticeService;

    // 경로 변경: noticeNo를 가진 공지사항 상세보기
    @GetMapping("/schedule/notice/{noticeNo}")
    public String viewNoticeDetail(@PathVariable("noticeNo") Long noticeNo, Model model) {
        Notice notice = noticeService.findByNoticeNo(noticeNo);
        if (notice == null) {
            throw new RuntimeException("공지사항을 찾을 수 없습니다: " + noticeNo);
        }
        model.addAttribute("notice", notice);
        return "notice/noticeDetail"; // 공지사항 상세 페이지로 리다이렉트
    }
    // 현재 로그인된 사용자 정보 가져오기 (중복 코드 제거)
    private String getLoggedInUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @PostMapping("/calendar/schedule/createScheduleWithJson")
    @ResponseBody
    public Map<String, String> createScheduleWithJson(@RequestBody ScheduleDto scheduleDto) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            String username = getLoggedInUsername();

            // 로그인된 사용자의 Member 정보 가져오기
            Member loggedInMember = memberRepository.findByMemId(username)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));

            // scheduleDto에 로그인된 사용자의 Member 설정
            scheduleDto.setMember(loggedInMember);

            // 일정 생성
            ScheduleDto createdSchedule = scheduleService.createSchedule(scheduleDto);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "일정이 성공적으로 등록되었습니다.");
            resultMap.put("schedule_no", createdSchedule.getSchedule_no().toString());
        } catch (Exception e) {
            resultMap.put("res_code", "500"); // 내부 서버 오류 코드 사용
            resultMap.put("res_msg", "일정 등록 중 오류가 발생했습니다. 상세 오류: " + e.getMessage()); // 상세 오류 메시지 추가
        }
        return resultMap;
    }

    // 로그인된 사용자의 일정 목록 반환
    @GetMapping("/calendar/schedule/getScheduleListForLoggedInUser")
    @ResponseBody
    public List<ScheduleDto> getScheduleListForLoggedInUser() {
        String username = getLoggedInUsername();
        return scheduleService.getAllSchedulesAndNotices(username); // 모든 일정과 공지사항을 반환하도록 수정
    }

    // 일정 수정
    @PostMapping("/calendar/schedule/update/{id}")
    @ResponseBody
    public Map<String, String> updateSchedule(
            @PathVariable("id") Long id,
            @RequestBody ScheduleDto scheduleDto) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            scheduleService.updateSchedule(id, scheduleDto);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "일정이 성공적으로 수정되었습니다.");
        } catch (RuntimeException e) {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "일정을 찾을 수 없습니다.");
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "일정 수정 중 오류가 발생했습니다. 상세 오류: " + e.getMessage());
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
        } catch (RuntimeException e) {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "일정을 찾을 수 없습니다.");
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "일정 삭제 중 오류가 발생했습니다. 상세 오류: " + e.getMessage());
        }
        return resultMap;
    }
}