package com.ware.spring.authorization.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;  
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ware.spring.approval_route.domain.ApprovalRoute;
import com.ware.spring.approval_route.domain.ApprovalRouteDto;
import com.ware.spring.approval_route.repository.ApprovalRouteRepository;
import com.ware.spring.approval_route.service.ApprovalRouteService;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.domain.AuthorizationDto;
import com.ware.spring.authorization.repository.AuthorizationRepository;
import com.ware.spring.authorization.service.AuthorizationService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.member.service.MemberService;
import com.ware.spring.security.vo.SecurityUser;

@Controller
public class AuthorizationViewController {

    private final AuthorizationService authorizationService;
    private final MemberService memberService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationViewController.class);
    private final AuthorizationRepository authorizationRepository;
    private final MemberRepository memberRepository;
    private final ApprovalRouteRepository approvalRouteRepository;
    private final ApprovalRouteService approvalRouteService; 
    
    @Autowired
    public AuthorizationViewController(AuthorizationService authorizationService, 
                                       MemberService memberService,
                                       AuthorizationRepository authorizationRepository
                                       , MemberRepository memberRepository
                                       , ApprovalRouteRepository approvalRouteRepository
                                       , ApprovalRouteService approvalRouteService) {
        this.authorizationService = authorizationService;
        this.memberService = memberService;
        this.authorizationRepository = authorizationRepository;
        this.memberRepository = memberRepository;
        this.approvalRouteRepository = approvalRouteRepository;
        this.approvalRouteService = approvalRouteService;
    }

    // 결재 리스트 조회
//    @GetMapping("/authorization/authorizationList")
//    public String listAuthorizations(
//            @RequestParam(value = "draftPage", defaultValue = "0") int draftPage,
//            @RequestParam(value = "completedPage", defaultValue = "0") int completedPage,
//            Model model) {
//
//        // 기안 진행 목록 페이지 처리 (authorRegDate 기준으로 내림차순 정렬)
//        Pageable draftPageable = PageRequest.of(draftPage, 5, Sort.by(Sort.Direction.DESC, "authorRegDate"));
//        Page<Authorization> resultList = authorizationService.getDraftAuthorizations(draftPageable);
//
//        // 완료 문서 페이지 처리 (authorRegDate 기준으로 내림차순 정렬)
//        Pageable completedPageable = PageRequest.of(completedPage, 5, Sort.by(Sort.Direction.DESC, "authorRegDate"));
//        Page<Authorization> completedList = authorizationService.getCompletedAuthorizations(completedPageable);
//
//        // 모델에 추가 (getContent()로 리스트만 가져옴)
//        model.addAttribute("resultList", resultList.getContent());
//        model.addAttribute("draftPage", resultList); // Page 객체 전달
//        model.addAttribute("completedList", completedList.getContent());
//        model.addAttribute("completedPage", completedList); // Page 객체 전달
//
//        return "authorization/authorizationList";
//    }
    
    
    @GetMapping("/authorization/authorizationList")
    public String listAuthorizations(
            @RequestParam(value = "draftPage", defaultValue = "0") int draftPage,
            @RequestParam(value = "completedPage", defaultValue = "0") int completedPage,
            Model model, Principal principal) {

        try {
            // 로그인한 사용자의 memNo 가져오기
            String memName = principal.getName();
            Optional<Member> memberOpt = memberRepository.findByMemId(memName);  // memName이 실제로는 memId를 의미

            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                Long memNo = member.getMemNo();  // memNo 가져오기

                // 기안 진행 중 문서 페이지 처리
                Pageable draftPageable = PageRequest.of(draftPage, 5, Sort.by(Sort.Direction.DESC, "authorRegDate"));
                Page<AuthorizationDto> resultList = authorizationService.getDraftAuthorizations(memNo, draftPageable);

                // 완료된 문서 페이지 처리
                Pageable completedPageable = PageRequest.of(completedPage, 5, Sort.by(Sort.Direction.DESC, "authorRegDate"));
                Page<AuthorizationDto> completedList = authorizationService.getCompletedAuthorizations(memNo, completedPageable);

                // 모델에 추가 (getContent()로 리스트만 가져옴)
                model.addAttribute("resultList", resultList.getContent());
                model.addAttribute("draftPage", resultList);
                model.addAttribute("completedList", completedList.getContent());
                model.addAttribute("completedPage", completedList);
            } else {
                System.out.println("Member with name " + memName + " not found.");
                return "error";  // 오류 페이지로 이동
            }

            return "authorization/authorizationList";

        } catch (Exception e) {
            e.printStackTrace();  // 예외 발생 시 스택 트레이스 출력
            return "error";  // 오류 페이지로 이동
        }
    }





    @GetMapping("/authorization/authorizationCreate")
    public String createAuthorizationPage(Model model, Principal principal) {
        // 로그인한 사용자의 사용자명(ID)을 가져옴
        String memName = principal.getName();  
        
        // 사용자명으로 Member 조회
        Optional<Member> memberOpt = memberRepository.findByMemName(memName);  
        
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            // empNo와 memNo를 모델에 추가하여 Thymeleaf 템플릿에서 사용할 수 있도록 설정
            model.addAttribute("empNo", member.getEmpNo()); // 사번
            model.addAttribute("memberNo", member.getMemNo()); // 멤버 번호
            System.out.println("empNo: " + member.getEmpNo()); // 디버깅 로그 추가
            System.out.println("memberNo: " + member.getMemNo()); // 디버깅 로그 추가
        } else {
            System.out.println("Member with name " + memName + " not found.");
        }

        // Model 확인을 위한 디버깅 로그 추가
        model.asMap().forEach((key, value) -> {
            System.out.println("Model attribute: " + key + " = " + value);
        });

        return "authorization/authorizationCreate";
    }

    
    
    // 모달 전달
    @GetMapping("/authorization/authorizationModal")
    public String showAuthorizationModal() {
        return "authorization/authorizationModal";
    }

    // 결재 문서함
    @GetMapping("/authorization/authorizationDocument")
    public String showAuthorizationDocument() {
        return "authorization/authorizationDocument";
    }

    // 문서함 연차 결재 서류 정보
    @GetMapping("/authorization/authorizationOff")
    public String showAuthorizationOffPage() {
        return "authorization/authorizationOff";
    }

    // 문서함 조퇴 결재 서류 정보
    @GetMapping("/authorization/authorizationLate")
    public String showAuthorizationLatePage() {
        return "authorization/authorizationLate";
    }

    // 문서함 해외 결재 서류 정보
    @GetMapping("/authorization/authorizationTrip")
    public String showAuthorizationTripPage() {
        return "authorization/authorizationTrip";
    }
    
    // 문서함 외근 신청서
    @GetMapping("/authorization/authorizationOutside")
    public String showAuthorizationOutsidePage() {
        return "authorization/authorizationOutside";
    }
        
    // 문서함 야근 신청서
    @GetMapping("/authorization/authorizationOvertime")
    public String showAuthorizationOvertimePage() {
        return "authorization/authorizationOvertime";
    }

    // 임시 저장함
    @GetMapping("/authorization/authorizationStorage")
    public String selectTemporaryAuthorizationList(
            @PageableDefault(size = 5) Pageable pageable, 
            Model model, 
            Principal principal) {
        
        // 로그인한 사용자의 empNo 가져오기
        String memName = principal.getName();
        Optional<Member> memberOpt = memberRepository.findByMemName(memName);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            model.addAttribute("empNo", member.getEmpNo()); // empNo를 모델에 추가
        } else {
            System.out.println("Member with name " + memName + " not found.");
        }

        // 정렬을 포함한 Pageable 객체 생성 (authorRegDate 기준 내림차순 정렬)
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "authorRegDate"));

        // 페이징 처리된 임시 저장 문서 리스트 가져오기
        Page<AuthorizationDto> tempListPage = authorizationService.selectTemporaryAuthorizationList(sortedPageable);

        // 페이징된 리스트를 모델에 추가
        model.addAttribute("tempListPage", tempListPage);
        model.addAttribute("currentPage", tempListPage.getNumber() + 1); // 현재 페이지
        model.addAttribute("totalPages", tempListPage.getTotalPages()); // 전체 페이지 수

        return "authorization/authorizationStorage";
    }




    
    // authorNo를 이용하여 문서 상세 페이지로 이동
    @GetMapping("/authorization/storage/url")
    public String getAuthorizationStorageDetail(@RequestParam("authorNo") Long authorNo, Model model) {
        System.out.println("Received authorNo: " + authorNo);
        if (authorNo == null || authorNo <= 0) {
            model.addAttribute("errorMessage", "유효하지 않은 문서 번호입니다.");
            return "authorization/authorizationStorage"; 
        }

        Authorization authorization = authorizationService.getAuthorizationById(authorNo);
        model.addAttribute("authorization", authorization);

        // 필드 값이 null인 경우 기본 값 설정
        model.addAttribute("title", authorization.getAuthTitle() != null ? authorization.getAuthTitle() : "");
        model.addAttribute("leaveType", authorization.getLeaveType() != null ? authorization.getLeaveType() : "");
        model.addAttribute("startDate", authorization.getStartDate() != null ? authorization.getStartDate() : "");
        model.addAttribute("endDate", authorization.getEndDate() != null ? authorization.getEndDate() : "");

        // Double 타입의 경우 0.0을 기본값으로 설정
        model.addAttribute("startEndDate", authorization.getStartEndDate() != null ? authorization.getStartEndDate() : 0.0);

        // 문서 타입에 따라 리다이렉트
        String docType = authorization.getDoctype();
        switch (docType) {
            case "off Report":
                return "authorization/authorizationStorageOff";
            case "late Report":
                return "authorization/authorizationStorageLate";
            case "overtime Report":
                return "authorization/authorizationStorageOvertime";
            case "outside Report":
                return "authorization/authorizationStorageOutside";
            case "trip Report":
                return "authorization/authorizationStorageTrip";
            default:
                return "authorization/authorizationStorageDetail";
        }
    }



	// 결재 확인 관련 결재자, 참조자 승인 확인란
    @GetMapping("/authorization/authorizationCheck")
    public String selectApprovalList(
            @RequestParam(value = "page", defaultValue = "0") int page,  // 페이지 번호를 받음
            @PageableDefault(size = 5) Pageable pageable,  // 페이지당 5개의 항목
            Model model
    ) {
        // 현재 로그인한 사용자의 ID를 SecurityContextHolder로부터 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String memId = authentication.getName();  // 현재 로그인한 사용자의 ID를 가져옴
            System.out.println("로그인된 사용자 ID: " + memId);
            
            // 최신순으로 정렬된 Pageable 객체 생성
            Pageable sortedPageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "authorRegDate"));


            // 서비스 메서드 호출 시 memId 전달, 페이징 처리된 결과를 받음
            Page<AuthorizationDto> authorizationPage = authorizationService.selectAuthorizationListForApproversAndReferers(memId, pageable);

            // 각 AuthorizationDto에 결재 경로 정보 추가
            for (AuthorizationDto authorization : authorizationPage.getContent()) {
                // ApprovalRoute 리스트를 가져옴
                List<ApprovalRoute> approvalRoutes = approvalRouteRepository.findByAuthorization_AuthorNo(authorization.getAuthorNo());

                // ApprovalRoute 리스트를 ApprovalRouteDto 리스트로 변환
                List<ApprovalRouteDto> approvalRouteDtos = approvalRoutes.stream()
                        .map(route -> ApprovalRouteDto.toDto(route, route.getMember(), null)) // 필요한 경우 Member 객체를 전달
                        .collect(Collectors.toList());

                // ApprovalRouteDto 리스트를 AuthorizationDto에 설정
                authorization.setApprovalRoutes(approvalRouteDtos);
            
                // `memNo`를 사용하여 Member를 조회하도록 변경
                Optional<Member> memberOptional = memberRepository.findByMemNo(authorization.getMemNo());
  
                if (memberOptional.isPresent()) {
                    Member member = memberOptional.get();
                    authorization.setMemName(member.getMemName()); // memName을 직접 설정
                } else {
                    System.out.println("해당 memNo에 대한 Member가 없습니다: " + authorization.getMemNo());
                }
            }
 
            // 모델에 페이징된 결재 내역 리스트 추가
            model.addAttribute("authorizationPage", authorizationPage);
            model.addAttribute("currentPage", authorizationPage.getNumber() + 1);  // 현재 페이지 번호
            model.addAttribute("totalPages", authorizationPage.getTotalPages());   // 전체 페이지 수

            return "authorization/authorizationCheck";
        } else {
            System.out.println("로그인된 사용자 정보가 없습니다.");
            return "redirect:/login";  // 로그인 페이지로 리다이렉트
        }
    }
    

    // 기안 진행 목록 가져오기
    @GetMapping("/authorization/drafts")
    @ResponseBody
    public List<AuthorizationDto> getDraftDocuments() {
        return authorizationService.selectDraftAuthorizationList();
    }
    
    // 기안 진행 완료 목록 가져오기
//    @GetMapping("/authorization/completed")
//    @ResponseBody
//    public List<AuthorizationDto> getcompletedDocuments() {
//        return authorizationService.selectCompletedAuthorizationList();
//    }
    
    // 완료된 문서 목록 조회 API
    @GetMapping("/api/authorization/completed")
    @ResponseBody
    public List<AuthorizationDto> getCompletedDocuments() {
        return authorizationService.selectCompletedAuthorizationList();
        
    }

    // 개별 문서 조회 API
    @GetMapping("/api/authorization/completed/{authorNo}")
    @ResponseBody
    public AuthorizationDto getCompletedAuthorizationById(@PathVariable("authorNo") Long authorNo) {
        Authorization authorization = authorizationRepository.findByAuthorNo(authorNo);
        
        if (authorization == null) {
            throw new IllegalArgumentException("Authorization not found: " + authorNo);
        }

        AuthorizationDto authorizationDto = AuthorizationDto.toDto(authorization);

        // 결재 경로 추가
        List<ApprovalRouteDto> approvalRouteDtos = approvalRouteRepository.findByAuthorization_AuthorNo(authorization.getAuthorNo())
                .stream()
                .map(route -> {
                    ApprovalRouteDto dto = ApprovalRouteDto.toDto(route);
                    
                    // 결재자 서명 추가
                    if ("Y".equals(route.getIsApprover())) {
                        dto.setApproverSignature(route.getApproverSignature());
                    }

                    // 참조자 서명 추가
                    if ("Y".equals(route.getIsReferer())) {
                        dto.setRefererSignature(route.getRefererSignature());
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        authorizationDto.setApprovalRoutes(approvalRouteDtos); // DTO에 결재 경로 추가
        return authorizationDto;
    }

    
    // 알림 정보 추가하는 메서드
    private void addNavDataToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memId = authentication.getName();
        Optional<Member> memberOpt = memberRepository.findByMemId(memId);

        if (memberOpt.isPresent()) {
            Long memNo = memberOpt.get().getMemNo();
            boolean approvalNotification = approvalRouteService.hasApprovalNotifications(memNo);
            boolean authorNotification = authorizationService.hasAuthorNotifications(memNo);

            // 알림 정보를 모델에 추가
            model.addAttribute("approvalNotification", approvalNotification);
            model.addAttribute("authorNotification", authorNotification);
        }
    }

}
