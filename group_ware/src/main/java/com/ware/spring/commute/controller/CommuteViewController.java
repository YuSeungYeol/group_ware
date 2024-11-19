package com.ware.spring.commute.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ware.spring.commute.service.CommuteService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.member.service.MemberService;
import com.ware.spring.security.vo.SecurityUser;

@Controller
public class CommuteViewController {

    private final CommuteService commuteService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    public CommuteViewController(MemberRepository memberRepository, MemberService memberService, CommuteService commuteService) {
        this.commuteService = commuteService;
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }

    /**
     * 출근 페이지를 표시합니다.
     * 설명: 로그인한 사용자의 정보를 모델에 추가하여 출근 페이지로 이동합니다.
     * 
     * @param securityUser 현재 로그인된 사용자 정보
     * @param model 뷰에 전달할 데이터
     * @return 출근 페이지 뷰 (commute/commute)
     */
    @GetMapping("/commute")
    public String commutePage(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        Member member = securityUser.getMember();
        model.addAttribute("member", member);
        return "commute/commute";
    }

    /**
     * 월별 개인 근태 상태를 조회합니다.
     * 설명: 로그인된 사용자의 연도별 월별 근태 정보를 조회하고, JSON 형식으로 변환하여 모델에 추가합니다.
     * 
     * @param principal 현재 로그인된 사용자 정보
     * @param model 뷰에 전달할 데이터
     * @param year 조회할 연도 (기본값: 현재 연도)
     * @return 월별 개인 근태 상태 뷰 (commute/commute_status_single)
     */
    @GetMapping("/commute/status_single")
    public String showMonthlyCommuteStatusSingle(Principal principal, Model model, @RequestParam(value = "year", required = false) Integer year) {
        String memId = principal.getName();
        Long memNo = commuteService.getMemberNoByUsername(memId);

        if (year == null) {
            year = java.time.Year.now().getValue();
        }

        Map<Integer, Map<String, Integer>> monthlyWorkingTime = commuteService.getMonthlyWorkingTime(memNo, year);
        int totalWorkingTime = commuteService.getTotalWorkingTime(memNo, year);
        int totalLateCount = commuteService.getTotalLateCount(memNo, year);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            model.addAttribute("year", year);
            model.addAttribute("monthlyWorkingTimeJson", objectMapper.writeValueAsString(monthlyWorkingTime));
            model.addAttribute("totalWorkingTime", totalWorkingTime);
            model.addAttribute("totalLateCount", totalLateCount);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("monthlyWorkingTimeJson", "{}");
            model.addAttribute("totalWorkingTime", 0);
            model.addAttribute("totalLateCount", 0);
        }

        return "/commute/commute_status_single";
    }

    /**
     * 근태 관리 페이지에 회원 목록을 표시합니다.
     * 설명: 상태 필터와 검색 조건에 따라 회원 목록을 조회하여 모델에 추가하고, 페이지네이션 처리합니다.
     * 
     * @param statusFilter 상태 필터 (예: 재직 중, 퇴사 등)
     * @param searchType 검색 유형 (예: 이름, 직급 등)
     * @param searchText 검색어
     * @param page 현재 페이지 번호
     * @param model 뷰에 전달할 데이터
     * @param securityUser 현재 로그인된 사용자 정보
     * @return 회원 목록 뷰 (commute/commute_list)
     */
    @GetMapping("/commute/list")
    public String listMembers(
            @RequestParam(value = "statusFilter", required = false, defaultValue = "active") String statusFilter,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Member> members;

        if (searchText != null && !searchText.isEmpty()) {
            Long currentUserDistributorNo = securityUser.getMember().getDistributor().getDistributorNo();
            members = memberService.searchMembersByCriteria(searchType, searchText, statusFilter, currentUserDistributorNo, pageable);
        } else {
            if ("mybranch".equals(statusFilter)) {
                Long currentUserDistributorNo = securityUser.getMember().getDistributor().getDistributorNo();
                members = memberService.findMembersByCurrentDistributor(currentUserDistributorNo, pageable);
            } else if ("resigned".equals(statusFilter)) {
                members = memberService.findAllByMemLeaveOrderByEmpNoAsc("Y", pageable);
            } else if ("all".equals(statusFilter)) {
                members = memberService.findAllOrderByEmpNoAsc(pageable);
            } else {
                members = memberService.findAllByMemLeaveOrderByEmpNoAsc("N", pageable);
            }
        }

        int totalPages = members.getTotalPages();
        int pageNumber = members.getNumber();
        int pageGroupSize = 5;
        int currentGroup = (pageNumber / pageGroupSize);
        int startPage = currentGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("memberList", members.getContent());
        model.addAttribute("page", members);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchText", searchText);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "commute/commute_list";
    }

    /**
     * 특정 회원의 상세 근태 기록을 조회합니다.
     * 설명: 주별, 연간 근무 기록 및 총 근무 시간과 지각 횟수를 조회하여 모델에 추가합니다.
     * 
     * @param memNo 회원 번호
     * @param model 뷰에 전달할 데이터
     * @param year 조회할 연도 (기본값: 현재 연도)
     * @param weekOffset 주차 이동 값 (기본값: 0)
     * @param monthOffset 월 이동 값 (기본값: 0)
     * @return 회원의 근태 상세 정보 뷰 (commute/commute_detail)
     */
    @GetMapping("/commute/detail/{memNo}")
    public String showCommuteList(@PathVariable("memNo") Long memNo, Model model, @RequestParam(value = "year", required = false) Integer year, 
                                  @RequestParam(value = "weekOffset", required = false, defaultValue = "0") int weekOffset,
                                  @RequestParam(value = "monthOffset", required = false, defaultValue = "0") int monthOffset) {
        if (year == null) {
            year = java.time.Year.now().getValue();
        }

        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        List<Map<String, Object>> weeklyWorkingTime = commuteService.getWeeklyWorkingTimeSummary(memNo, weekOffset);
        Map<String, Object> annualWorkingTime = commuteService.getAnnualWorkingTimeSummary(memNo, year);
        int totalWorkingTime = commuteService.getTotalWorkingTime(memNo, year);
        int totalLateCount = commuteService.getTotalLateCount(memNo, year);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            model.addAttribute("year", year);
            model.addAttribute("weeklyWorkingTimeJson", objectMapper.writeValueAsString(weeklyWorkingTime));
            model.addAttribute("annualWorkingTimeJson", objectMapper.writeValueAsString(annualWorkingTime));
            model.addAttribute("totalWorkingTime", totalWorkingTime);
            model.addAttribute("totalLateCount", totalLateCount);
            model.addAttribute("member", member);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("weeklyWorkingTimeJson", "{}");
            model.addAttribute("monthlyWorkingTimeJson", "{}");
            model.addAttribute("annualWorkingTimeJson", "{}");
            model.addAttribute("totalWorkingTime", 0);
            model.addAttribute("totalLateCount", 0);
            model.addAttribute("member", member);
        }

        return "commute/commute_detail";
    }
}
