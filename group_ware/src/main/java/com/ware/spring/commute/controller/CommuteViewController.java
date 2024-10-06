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
	    public CommuteViewController(MemberRepository memberRepository,MemberService memberService,CommuteService commuteService) {
	        this.commuteService = commuteService;
	        this.memberService = memberService;
	        this.memberRepository = memberRepository;

	    }
    // 로그인한 사용자 정보를 불러와 출근 페이지로 전달
    @GetMapping("/commute")
    public String commutePage(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        Member member = securityUser.getMember();
        model.addAttribute("member", member);
        return "commute/commute";  // commute.html로 이동
    }


    @GetMapping("/commute/status_single")
    public String showMonthlyCommuteStatusSingle(Principal principal, Model model, @RequestParam(value = "year", required = false) Integer year) {
        // 로그인된 사용자의 정보에서 memNo 가져오기
        String memId = principal.getName();
        Long memNo = commuteService.getMemberNoByUsername(memId);

        // 연도 기본 값 설정 (현재 연도)
        if (year == null) {
            year = java.time.Year.now().getValue();
        }

        // 연도별 월별 근무 시간 및 총 지각 횟수 데이터 가져오기
        Map<Integer, Map<String, Integer>> monthlyWorkingTime = commuteService.getMonthlyWorkingTime(memNo, year);
        int totalWorkingTime = commuteService.getTotalWorkingTime(memNo, year);
        int totalLateCount = commuteService.getTotalLateCount(memNo, year);

        // 데이터를 JSON으로 변환하여 모델에 추가
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

        // 검색어가 있으면 필터 + 검색어로 결과를 조회, 없으면 필터만 적용된 결과 조회
        if (searchText != null && !searchText.isEmpty()) {
            Long currentUserDistributorNo = securityUser.getMember().getDistributor().getDistributorNo();
            members = memberService.searchMembersByCriteria(searchType, searchText, statusFilter, currentUserDistributorNo, pageable);
        } else {
            // 검색어가 없을 경우 필터링만 적용
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

        // 페이지네이션 처리
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
    @GetMapping("/commute/detail/{memNo}")
    public String showCommuteList(@PathVariable("memNo") Long memNo, Model model, @RequestParam(value = "year", required = false) Integer year, 
            @RequestParam(value = "weekOffset", required = false, defaultValue = "0") int weekOffset,
            @RequestParam(value = "monthOffset", required = false, defaultValue = "0") int monthOffset) {
	if (year == null) {
	year = java.time.Year.now().getValue();
	}
	
	// memNo를 사용하여 회원 정보를 가져옴
	Member member = memberRepository.findById(memNo)
	.orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
	
	// 주간 근무 기록 가져오기
	List<Map<String, Object>> weeklyWorkingTime = commuteService.getWeeklyWorkingTimeSummary(memNo, weekOffset);
	

	
	// 연간 근무 기록 가져오기
	Map<String, Object> annualWorkingTime = commuteService.getAnnualWorkingTimeSummary(memNo, year);
	
	// 총 근무 시간과 총 지각 횟수 가져오기
	int totalWorkingTime = commuteService.getTotalWorkingTime(memNo, year);
	int totalLateCount = commuteService.getTotalLateCount(memNo, year);
	
	// JSON 변환 및 모델 추가
	ObjectMapper objectMapper = new ObjectMapper();
	try {
	model.addAttribute("year", year);
	model.addAttribute("weeklyWorkingTimeJson", objectMapper.writeValueAsString(weeklyWorkingTime));
	model.addAttribute("annualWorkingTimeJson", objectMapper.writeValueAsString(annualWorkingTime));
	model.addAttribute("totalWorkingTime", totalWorkingTime);
	model.addAttribute("totalLateCount", totalLateCount);
	model.addAttribute("member", member); // Member 정보를 추가
	} catch (Exception e) {
	e.printStackTrace();
	model.addAttribute("weeklyWorkingTimeJson", "{}");
	model.addAttribute("monthlyWorkingTimeJson", "{}");
	model.addAttribute("annualWorkingTimeJson", "{}");
	model.addAttribute("totalWorkingTime", 0);
	model.addAttribute("totalLateCount", 0);
	model.addAttribute("member", member); // Member 정보를 추가
	}

        return "commute/commute_detail";
    }

}
