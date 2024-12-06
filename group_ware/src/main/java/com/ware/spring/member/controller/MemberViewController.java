package com.ware.spring.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    public MemberViewController(MemberService memberService, MemberRepository memberRepository, RankRepository rankRepository, DistributorRepository distributorRepository, DistributorService distributorService) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.rankRepository = rankRepository;
        this.distributorRepository = distributorRepository;
        this.distributorService = distributorService;
    }

    /**
     * 로그인 페이지를 반환합니다.
     * 설명: 회원 로그인 페이지로 이동합니다.
     * 
     * @return 로그인 페이지 뷰 (member/member_login)
     */
    @GetMapping("/login")
    public String loginPage() {
        return "member/member_login"; 
    }

    /**
     * 회원 등록 페이지를 반환합니다.
     * 설명: 회원 등록에 필요한 직급 및 지점 정보를 모델에 추가하여 회원 등록 페이지로 이동합니다.
     * 
     * @param model 뷰에 전달할 데이터
     * @return 회원 등록 페이지 뷰 (member/member_register)
     */
    @GetMapping("/member/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("rank", memberService.getRank());
        model.addAttribute("distributors", memberService.getDistributors());
        return "member/member_register"; 
    }

    /**
     * 회원 가입 성공 페이지를 반환합니다.
     * 설명: 회원 가입이 완료된 후 성공 페이지로 이동합니다.
     * 
     * @return 성공 페이지 뷰 (home)
     */
    @GetMapping("/member/success")
    public String showSuccessPage() {
        return "home";  
    }

    /**
     * 마이페이지를 반환합니다.
     * 설명: 로그인한 사용자의 정보, 직급 및 지점 정보를 모델에 추가하여 마이페이지로 이동합니다.
     * 
     * @param securityUser 현재 로그인한 사용자
     * @param model 뷰에 전달할 데이터
     * @return 마이페이지 뷰 (member/member_mypage)
     */
    @GetMapping("/member/mypage")
    public String myPage(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        Long memNo = securityUser.getMember().getMemNo();
        model.addAttribute("rank", memberService.getRank());
        model.addAttribute("distributors", memberService.getDistributors());
        model.addAttribute("member", memberService.getMemberById(memNo));
        return "member/member_mypage"; 
    }

    /**
     * 회원 리스트 페이지를 반환합니다.
     * 설명: 회원의 상태 필터와 검색 조건을 기반으로 회원 목록을 조회하여 페이징 처리된 결과를 반환합니다.
     * 검색 조건이 있는 경우 검색어와 필터에 따라 회원 목록을 조회하고,
     * 검색 조건이 없는 경우 상태 필터에 따라 회원 목록을 필터링합니다.
     * 
     * @param statusFilter 회원 상태 필터 (active, resigned, all 등)
     * @param searchType 검색 유형 ('name', 'rank' 등)
     * @param searchText 검색어
     * @param page 페이지 번호 (0부터 시작)
     * @param model 뷰에 전달할 데이터
     * @param securityUser 현재 로그인한 사용자
     * @return 회원 리스트 페이지 뷰 (member/member_list)
     */
    @GetMapping("/member/list")
    public String listMembers(
            @RequestParam(value = "statusFilter", required = false, defaultValue = "active") String statusFilter,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "sortField", required = false, defaultValue = "empNo") String sortField1,
            @RequestParam(value = "sortField", required = false, defaultValue = "distributorName") String sortField2,
            @RequestParam(value = "sortField", required = false, defaultValue = "memName") String sortField3,
            @RequestParam(value = "sortField", required = false, defaultValue = "memRegDate") String sortField4,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField1, sortField2, sortField3, sortField4);
        Pageable pageable = PageRequest.of(page, 10, sort);

        Page<Member> members;

        if (searchText != null && !searchText.isEmpty()) {
            Long distributorNo = securityUser.getMember().getDistributor().getDistributorNo();
            members = memberService.searchMembersByCriteria(searchType, searchText, statusFilter, distributorNo, pageable);
        } else {
         
            switch (statusFilter) {
                case "mybranch":
                    Long distributorNo = securityUser.getMember().getDistributor().getDistributorNo();
                    members = memberService.findMembersByCurrentDistributor(distributorNo, pageable);
                    break;
                case "resigned":
                    members = memberService.findAllByMemLeaveOrderByEmpNoAsc("Y", pageable);
                    break;	
                case "all":
                    members = memberService.findAllOrderByEmpNoAsc(pageable);
                    break;
                default: 
                    members = memberService.findAllByMemLeaveOrderByEmpNoAsc("N", pageable);
                    break;
            }
        }
        int totalPages = members.getTotalPages();
        int pageNumber = members.getNumber();
        int pageGroupSize = 5;
        int currentGroup = pageNumber / pageGroupSize;
        int startPage = currentGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);
        model.addAttribute("memberList", members.getContent());
        model.addAttribute("page", members);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchText", searchText);
        model.addAttribute("sortField", sortField1);
        model.addAttribute("sortField", sortField2);
        model.addAttribute("sortField", sortField3);
        model.addAttribute("sortField", sortField4);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "member/member_list";
    }

    /**
     * 회원 상세 정보 페이지를 반환합니다.
     * 설명: 주어진 회원 ID를 기반으로 특정 회원의 정보를 조회하여 모델에 추가합니다.
     * 모든 직급과 지점 정보도 함께 조회하여 뷰에 전달합니다.
     * 
     * @param id 회원 ID
     * @param model 뷰에 전달할 데이터
     * @return 회원 상세 정보 페이지 뷰 (member/member_info)
     */
    @GetMapping("/member/detail/{id}")
    public String getMemberDetail(@PathVariable("id") Long id, Model model) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
        
        List<Rank> rankList = rankRepository.findAll();
        List<Distributor> distributorList = distributorRepository.findAll();

        model.addAttribute("member", member);
        model.addAttribute("rankList", rankList);
        model.addAttribute("distributorList", distributorList);

        return "member/member_info";
    }
}
