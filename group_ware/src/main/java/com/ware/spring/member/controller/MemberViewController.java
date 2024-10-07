package com.ware.spring.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ware.spring.commute.domain.Commute;
import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.domain.Rank;
import com.ware.spring.member.repository.DistributorRepository;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.member.repository.RankRepository;
import com.ware.spring.member.service.DistributorService;
import com.ware.spring.member.service.MemberService;
import com.ware.spring.security.vo.SecurityUser;

@Controller
public class MemberViewController {

    private final MemberService memberService;

    private final MemberRepository memberRepository;
    private final RankRepository rankRepository;
    private final DistributorRepository distributorRepository;
    private final DistributorService distributorService; 

    @Autowired
    public MemberViewController(MemberService memberService,MemberRepository memberRepository, RankRepository rankRepository, DistributorRepository distributorRepository
    		,DistributorService distributorService) {
        this.memberRepository = memberRepository;
        this.rankRepository = rankRepository;
        this.distributorRepository = distributorRepository;
        this.memberService = memberService;
        this.distributorService = distributorService;
    }

    
    @GetMapping("/login")
    public String loginPage() {
        return "member/member_login"; 
    }

    @GetMapping("/member/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("rank", memberService.getRank());
        model.addAttribute("distributors", memberService.getDistributors());
        return "member/member_register"; 
    }

    @GetMapping("/member/success")
    public String showSuccessPage() {
        return "home";  
    }
    @GetMapping("/member/mypage")
    public String myPage(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        // 현재 로그인한 사용자의 정보를 가져와서 모델에 추가
        Long memNo = securityUser.getMember().getMemNo();
        model.addAttribute("rank", memberService.getRank());
        model.addAttribute("distributors", memberService.getDistributors());
        model.addAttribute("member", memberService.getMemberById(memNo));
        return "member/member_mypage";  // member_mypage.html 뷰를 반환
    }
    @GetMapping("/member/list")
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

        return "member/member_list";
    }




    @GetMapping("/member/detail/{id}")
    public String getMemberDetail(@PathVariable("id") Long id, Model model) {
        // 주어진 ID로 Member 객체를 찾음
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        // 모든 Rank와 Distributor 정보도 함께 불러와서 뷰로 전달
        List<Rank> rankList = rankRepository.findAll();  // 모든 직급 정보
        List<Distributor> distributorList = distributorRepository.findAll();  // 모든 지점 정보

        // Model에 데이터를 추가하여 뷰로 전달
        model.addAttribute("member", member);  // 특정 회원 정보
        model.addAttribute("rankList", rankList);  // 직급 리스트
        model.addAttribute("distributorList", distributorList);  // 지점 리스트

        // member_info.html로 이동
        return "member/member_info";
    }



}

