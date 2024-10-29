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
    
    /**
     * 기안 진행 중 및 완료된 문서 목록을 조회하여 화면에 표시하는 메서드.
     * 
     * ## 기능
     * - 로그인한 사용자의 기안 진행 중 문서와 완료된 문서 리스트를 페이지 단위로 조회하여 모델에 추가
     * 
     * ## 기술
     * - Spring Data JPA를 사용하여 기안 상태에 따른 문서 리스트를 `Pageable`로 조회
     * - GET 요청에 따라 뷰에 필요한 데이터(페이지 번호, 문서 리스트)를 Model에 담아 반환
     * 
     * @param draftPage - 진행 중인 문서의 페이지 번호, 기본값은 0
     * @param completedPage - 완료된 문서의 페이지 번호, 기본값은 0
     * @param model - 뷰로 전달할 데이터를 담는 Model 객체
     * @param principal - 현재 로그인한 사용자의 정보를 포함하는 Principal 객체
     * @return String - 문서 목록 화면을 나타내는 뷰 이름, 예외 시 오류 페이지 반환
     */
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

    /**
     * 문서 생성 페이지를 로드하여 사용자 정보를 모델에 추가하는 메서드.
     * 
     * ## 기능
     * - 로그인한 사용자의 empNo(사번)와 memNo(멤버 번호)를 조회하여 모델에 추가
     * - Thymeleaf 템플릿에서 사용자 정보를 활용할 수 있도록 설정
     * 
     * ## 기술
     * - Spring Security를 사용해 로그인한 사용자 정보를 Principal로 받아옴
     * - Spring Data JPA를 통해 Member 정보를 조회하여 모델에 담음
     * 
     * @param model - 뷰로 전달할 데이터를 담는 Model 객체
     * @param principal - 현재 로그인한 사용자의 정보를 포함하는 Principal 객체
     * @return String - 문서 생성 화면을 나타내는 뷰 이름
     */
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

    /**
     * 결재 모달 창을 로드하는 메서드.
     * 
     * ## 기능
     * - 사용자에게 결재 관련 모달 창을 표시하기 위해 뷰를 반환
     * - 추가적인 데이터나 로직 없이 단순하게 모달 뷰로 이동
     * 
     * ## 기술
     * - Spring MVC의 @GetMapping을 사용하여 GET 요청을 처리
     * - 모달 창은 Thymeleaf 템플릿을 통해 프론트엔드에 렌더링됨
     * 
     * @return String - 결재 모달 화면을 나타내는 뷰 이름
     */
    @GetMapping("/authorization/authorizationModal")
    public String showAuthorizationModal() {
        return "authorization/authorizationModal";
    }

    /**
     * 결재 문서함 페이지를 로드하는 메서드.
     * 
     * ## 기능
     * - 사용자에게 결재 문서함 화면을 표시
     * - 결재 문서 목록 및 관련 데이터가 포함된 페이지를 로드하기 위한 엔드포인트
     * 
     * ## 기술
     * - Spring MVC의 @GetMapping을 사용하여 GET 요청을 처리
     * - Thymeleaf 템플릿을 통해 프론트엔드에 결재 문서함 뷰를 렌더링
     * 
     * @return String - 결재 문서함 페이지를 나타내는 뷰 이름
     */
    @GetMapping("/authorization/authorizationDocument")
    public String showAuthorizationDocument() {
        return "authorization/authorizationDocument";
    }

    /**
     * 연차 결재 서류 정보를 보여주는 페이지를 로드하는 메서드.
     * 
     * ## 기능
     * - 사용자에게 연차 결재 서류 정보 화면을 제공
     * - 연차 신청, 확인 및 승인/반려와 관련된 정보가 포함된 페이지를 로드하기 위한 엔드포인트
     * 
     * ## 기술
     * - Spring MVC의 @GetMapping을 통해 연차 결재 서류 화면을 로드
     * - Thymeleaf 템플릿 엔진을 통해 프론트엔드에 뷰를 렌더링
     * 
     * @return String - 연차 결재 서류 정보 페이지를 나타내는 뷰 이름
     */
    @GetMapping("/authorization/authorizationOff")
    public String showAuthorizationOffPage() {
        return "authorization/authorizationOff";
    }

    /**
     * 조퇴 결재 서류 정보를 보여주는 페이지를 로드하는 메서드.
     * 
     * ## 기능
     * - 사용자에게 조퇴 결재 서류 정보 화면을 제공
     * - 조퇴 신청, 확인 및 승인/반려와 관련된 정보가 포함된 페이지를 로드하기 위한 엔드포인트
     * 
     * ## 기술
     * - Spring MVC의 @GetMapping을 통해 조퇴 결재 서류 화면을 로드
     * - Thymeleaf 템플릿 엔진을 통해 프론트엔드에 뷰를 렌더링
     * 
     * @return String - 조퇴 결재 서류 정보 페이지를 나타내는 뷰 이름
     */
    @GetMapping("/authorization/authorizationLate")
    public String showAuthorizationLatePage() {
        return "authorization/authorizationLate";
    }

    /**
     * 해외 결재 서류 정보를 보여주는 페이지를 로드하는 메서드.
     * 
     * ## 기능
     * - 사용자에게 해외 결재 서류 정보 화면을 제공
     * - 해외 출장 및 외부 근무와 관련된 결재 서류의 신청, 확인, 승인/반려와 관련된 정보가 포함된 페이지를 로드
     * 
     * ## 기술
     * - Spring MVC의 @GetMapping을 통해 해외 결재 서류 화면을 로드
     * - Thymeleaf 템플릿 엔진을 통해 프론트엔드에 뷰를 렌더링하여 사용자에게 제공
     * 
     * @return String - 해외 결재 서류 정보 페이지를 나타내는 뷰 이름
     */
    @GetMapping("/authorization/authorizationTrip")
    public String showAuthorizationTripPage() {
        return "authorization/authorizationTrip";
    }
    
    /**
     * 외근 신청서 페이지를 로드하는 메서드.
     * 
     * ## 기능
     * - 사용자에게 외근 신청서 문서를 조회 및 관리할 수 있는 화면을 제공
     * - 외근 신청, 수정, 승인/반려와 같은 외근 관련 결재 정보를 표시하는 페이지를 로드
     * 
     * ## 기술
     * - Spring MVC의 @GetMapping을 사용하여 외근 신청서 페이지를 호출
     * - Thymeleaf 템플릿 엔진을 이용해 외근 결재 서류의 사용자 인터페이스를 렌더링
     * 
     * @return String - 외근 신청서 페이지를 표시하는 뷰 이름
     */
    @GetMapping("/authorization/authorizationOutside")
    public String showAuthorizationOutsidePage() {
        return "authorization/authorizationOutside";
    }
        
    /**
     * 야근 신청서 페이지를 로드하는 메서드.
     * 
     * ## 기능
     * - 사용자에게 야근 신청서를 조회하고 관리할 수 있는 화면을 제공
     * - 야근 신청, 수정, 승인/반려 등 야근 관련 결재 정보를 표시하는 페이지를 로드
     * 
     * ## 기술
     * - Spring MVC의 @GetMapping을 사용하여 야근 신청서 페이지를 호출
     * - Thymeleaf 템플릿 엔진을 이용해 야근 신청서의 사용자 인터페이스를 렌더링
     * 
     * @return String - 야근 신청서 페이지를 표시하는 뷰 이름
     */
    @GetMapping("/authorization/authorizationOvertime")
    public String showAuthorizationOvertimePage() {
        return "authorization/authorizationOvertime";
    }

    /**
     * 임시 저장 문서 리스트를 조회하여 임시 저장함 페이지를 로드하는 메서드.
     *
     * ## 기능
     * - 로그인한 사용자의 임시 저장 문서 리스트를 페이징하여 가져옴
     * - empNo를 기반으로 사용자 정보를 확인하고, 페이징된 임시 저장 문서를 모델에 추가
     * - 임시 저장함 페이지에서 임시 저장된 결재 문서 리스트를 관리할 수 있도록 함
     *
     * ## 기술
     * - Spring MVC의 @GetMapping 및 @PageableDefault 애너테이션을 사용하여 페이징 처리
     * - Pageable 객체를 이용해 authorRegDate 기준 내림차순으로 정렬된 문서 목록을 페이징하여 조회
     * - Thymeleaf 템플릿에서 사용될 모델에 현재 페이지 및 전체 페이지 수를 포함하여 데이터 전달
     *
     * @param pageable 페이징 및 정렬 설정 객체
     * @param model Thymeleaf 모델 객체
     * @param principal 현재 로그인된 사용자의 인증 객체
     * @return String - 임시 저장함 페이지 뷰 이름
     */
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

    
    /**
     * authorNo를 이용하여 문서 상세 페이지로 이동하는 메서드.
     *
     * ## 기능
     * - 주어진 authorNo를 기반으로 문서의 상세 정보를 조회하고 모델에 추가
     * - 문서 타입에 따라 적절한 상세 페이지로 리다이렉트
     *
     * ## 기술
     * - 유효하지 않은 authorNo 처리: null 또는 0 이하일 경우 오류 메시지를 모델에 추가
     * - Authorization 엔티티를 조회하고 모델에 필드 값을 설정
     * - 각 문서 타입에 따라 반환되는 뷰를 다르게 설정
     *
     * @param authorNo 문서 번호
     * @param model Thymeleaf 모델 객체
     * @return String - 해당 문서의 상세 페이지 뷰 이름
     */
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

    /**
     * 결재 확인 관련 결재자 및 참조자의 승인 확인 리스트를 조회하는 메서드.
     *
     * ## 기능
     * - 현재 로그인한 사용자의 ID를 기반으로 결재 내역을 조회하고 모델에 추가
     * - 승인 내역을 페이지 처리하여 반환
     *
     * ## 기술
     * - SecurityContextHolder에서 로그인 정보를 가져와 사용자의 ID 확인
     * - 결재 내역을 페이지 단위로 조회하고, 각 결재 내역에 결재 경로 정보를 추가
     *
     * @param page 요청 페이지 번호
     * @param pageable 페이지 당 항목 수
     * @param model Thymeleaf 모델 객체
     * @return String - 결재 확인 페이지 뷰 이름
     */
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
    
    /**
     * 기안 진행 목록 가져오기 API
     *
     * ## 기능
     * - 현재 로그인한 사용자의 기안 문서 목록을 조회
     *
     * ## 기술
     * - SecurityContextHolder를 사용하여 로그인 정보를 가져와 현재 사용자의 ID를 확인
     * - AuthorizationService를 통해 기안 문서 목록을 조회
     *
     * @return List<AuthorizationDto> - 기안 문서 목록
     */
    @GetMapping("/authorization/drafts")
    @ResponseBody
    public List<AuthorizationDto> getDraftDocuments() {
        return authorizationService.selectDraftAuthorizationList();
    }
    
    /**
     * 완료된 문서 목록 조회 API
     *
     * ## 기능
     * - 현재 로그인한 사용자의 완료된 문서 목록을 조회하여 반환
     *
     * ## 기술
     * - AuthorizationService를 통해 완료된 문서 목록을 조회
     * - 결과는 AuthorizationDto 리스트 형태로 반환됨
     *
     * @return List<AuthorizationDto> - 완료된 문서 목록
     */
    @GetMapping("/api/authorization/completed")
    @ResponseBody
    public List<AuthorizationDto> getCompletedDocuments() {
        return authorizationService.selectCompletedAuthorizationList();
        
    }

    /**
     * 개별 문서 조회 API
     *
     * ## 기능
     * - 주어진 authorNo를 기반으로 완료된 문서를 조회하고 해당 문서의 결재 경로 정보를 포함하여 반환
     *
     * ## 기술
     * - AuthorizationRepository를 사용하여 authorNo에 해당하는 Authorization 객체를 검색
     * - 검색된 Authorization 객체가 존재하지 않을 경우 예외를 발생시킴
     * - 결재 경로 정보를 ApprovalRouteRepository를 통해 조회하고 DTO에 추가
     *
     * @param authorNo 문서의 고유 번호
     * @return AuthorizationDto - 완료된 문서의 DTO
     * @throws IllegalArgumentException 문서를 찾을 수 없는 경우
     */
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

    
    /**
     * 알림 정보 추가하는 메서드.
     *
     * ## 기능
     * - 현재 로그인한 사용자의 ID를 기반으로 알림 정보를 조회하고 모델에 추가
     * - 결재 알림 및 작성자 알림 여부를 확인하여 모델에 설정
     *
     * ## 기술
     * - SecurityContextHolder를 사용하여 현재 로그인한 사용자의 정보를 가져옴
     * - MemberRepository를 통해 사용자 정보를 조회하고, 해당 사용자의 번호(memNo)를 얻음
     * - ApprovalRouteService 및 AuthorizationService를 사용하여 알림 여부를 확인
     *
     * @param model Thymeleaf 모델 객체
     */
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
