package com.ware.spring.authorization.controller;

import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ware.spring.approval_route.domain.ApprovalRoute;
import com.ware.spring.approval_route.domain.ApprovalRouteDto;
import com.ware.spring.approval_route.repository.ApprovalRouteRepository;
import com.ware.spring.approval_route.service.ApprovalRouteService;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.domain.AuthorizationDto;
import com.ware.spring.authorization.repository.AuthorizationRepository;
import com.ware.spring.authorization.service.AuthorizationFileService;
import com.ware.spring.authorization.service.AuthorizationService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.member.service.MemberService;
import com.ware.spring.security.vo.SecurityUser;

@Controller
public class AuthorizationApiController {

    private final AuthorizationService authorizationService;
    private final AuthorizationFileService authorizationFileService;
    private final ApprovalRouteService approvalRouteService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final ApprovalRouteRepository approvalRouteRepository;
    private final AuthorizationRepository authorizationRepository;

    @Autowired
    public AuthorizationApiController(AuthorizationService authorizationService, 
                                      AuthorizationFileService authorizationFileService, ApprovalRouteService approvalRouteService, 
                                      MemberService memberService, MemberRepository memberRepository, ApprovalRouteRepository approvalRouteRepository
                                      ,AuthorizationRepository authorizationRepository) {
        this.authorizationService = authorizationService;
        this.authorizationFileService = authorizationFileService;
        this.approvalRouteService = approvalRouteService;
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.approvalRouteRepository = approvalRouteRepository;
        this.authorizationRepository = authorizationRepository;
    }

    @GetMapping("/download/{author_no}")
    public ResponseEntity<Object> boardImgDownload(@PathVariable("author_no") Long author_no) {
        return authorizationFileService.download(author_no);
    }
    /**
     * 결재 문서를 생성하는 API 엔드포인트.
     * 
     * 이 메서드는 사용자가 제출한 결재 문서 정보를 포함하는 DTO를 받아
     * 문서 제목, 내용, 결재자 및 참조자 정보를 설정합니다. 
     * 또한, 첨부 파일을 업로드하고, 다양한 문서 유형에 따라 필요한 추가 정보를 설정하여
     * 최종적으로 결재 문서를 데이터베이스에 등록합니다.
     * 
     * ## 기능
     * - 결재 문서 정보 DTO를 생성하고 설정
     * - 기안자 정보를 기반으로 결재 문서 생성
     * - 결재자 및 참조자 목록을 JSON 형식으로 받아 처리
     * - 선택적 파일 첨부 기능 제공
     * - 다양한 문서 유형에 따라 필수 정보 설정
     * 
     * ## 기술
     * - Spring Boot 프레임워크의 REST API 방식으로 구현
     * - Jackson 라이브러리를 사용하여 JSON 데이터 처리
     * - MultipartFile을 통해 파일 업로드 처리
     * - Service 레이어를 통해 비즈니스 로직 수행
     * 
     * @param dto 결재 문서 정보를 담고 있는 DTO
     * @param file 첨부할 파일 (선택적)
     * @param title 문서 제목
     * @param approversJson 결재자 ID의 JSON 문자열
     * @param referer 참조자 ID
     * @param docType 문서 타입
     * @param content 문서 내용
     * @param leaveType 휴가 유형 (선택적)
     * @param startDate 시작 날짜 (선택적)
     * @param endDate 종료 날짜 (선택적)
     * @param startEndDate 시작과 종료 날짜의 간격 (선택적)
     * @param lateType 지각 유형 (선택적)
     * @param tripType 출장 유형 (선택적)
     * @param outsideType 외출 유형 (선택적)
     * @param overtimeType 초과 근무 유형 (선택적)
     * @param memNo 기안자의 멤버 번호 (필수)
     * @return 문서 등록 결과를 포함하는 맵
     */
    @ResponseBody
    @PostMapping("/authorization")
    public Map<String, String> createAuthorization(
            AuthorizationDto dto, 
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("approvers") String approversJson,
            @RequestParam("referer") Long referer,
            @RequestParam("docType") String docType,
            @RequestParam("content") String content,
            @RequestParam(value = "leaveType", required = false) String leaveType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "startEndDate", required = false) Double startEndDate,
            @RequestParam(value = "lateType", required = false) String lateType,
            @RequestParam(value = "tripType", required = false) String tripType,
            @RequestParam(value = "outsideType", required = false) String outsideType,
            @RequestParam(value = "overtimeType", required = false) String overtimeType,
            @RequestParam(value = "memberNo", required = true) Long memNo) {

        System.out.println("Received DTO: " + dto);
        System.out.println("Received Title: " + title);
        System.out.println("Received Approvers JSON: " + approversJson);
        System.out.println("Received Referer: " + referer);
        System.out.println("Received DocType: " + docType);

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "문서 등록 중 오류가 발생하였습니다");

        try {
            // memberNo 설정
            dto.setMemNo(memNo);

            // Member 정보를 조회하여 memName, empNo, distributorNo 설정
            Optional<Member> memberOpt = memberRepository.findByMemNo(memNo);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                dto.setMemName(member.getMemName()); // 기안자 이름 설정
                dto.setEmpNo(member.getEmpNo()); // 사번 설정
                dto.setDistributorNo(member.getDistributor().getDistributorNo()); // 소속 설정
            } else {
                resultMap.put("res_msg", "해당 멤버를 찾을 수 없습니다.");
                return resultMap;
            }

            // 제목 설정
            dto.setAuthTitle(title);
            dto.setAuthContent(content);  // content 값을 authContent에 설정

            // 문서 타입 설정
            dto.setDoctype(docType);
            dto.setAuthorName(docType);  // 문서 타입을 작성자 이름에 임시 저장
            dto.setAuthorStatus("P");  // 대기 상태

            // 문서 타입에 따라 DTO 설정
            switch (docType) {
                case "off Report":
                    dto.setLeaveType(leaveType);
                    dto.setStartDate(startDate);
                    dto.setEndDate(endDate);
                    dto.setStartEndDate(startEndDate);
                    break;
                case "late Report":
                    dto.setLateType(lateType);
                    dto.setStartDate(startDate);
                    break;
                case "trip Report":
                    dto.setTripType(tripType);
                    dto.setStartDate(startDate);
                    dto.setEndDate(endDate);
                    break;
                case "outside Report":
                    dto.setOutsideType(outsideType);
                    dto.setStartDate(startDate);
                    dto.setEndDate(endDate);
                    break;
                case "overtime Report":
                    dto.setOvertimeType(overtimeType);
                    dto.setStartDate(startDate);
                    dto.setEndDate(endDate);
                    dto.setStartEndDate(startEndDate);
                    break;
                default:
                    throw new IllegalArgumentException("알 수 없는 문서 유형입니다: " + docType);
            }

            // 파일 업로드 처리
            if (file != null && !file.isEmpty()) {
                String savedFileName = authorizationFileService.upload(file);
                if (savedFileName != null) {
                    dto.setAuthorOriThumbnail(file.getOriginalFilename());
                    dto.setAuthorNewThumbnail(savedFileName);
                } else {
                    resultMap.put("res_msg", "파일 업로드 실패");
                    return resultMap;
                }
            }

            // approversJson을 List<Long>으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            List<Long> approversIds = Arrays.asList(objectMapper.readValue(approversJson, Long[].class));

            // List<Long>을 List<ApprovalRouteDto>로 변환
            List<ApprovalRouteDto> approvers = approversIds.stream()
                .map(id -> {
                    ApprovalRouteDto approverDto = new ApprovalRouteDto();
                    approverDto.setMemNo(id);
                    approverDto.setIsApprover("Y");
                    approverDto.setIsReferer("N");
                    return approverDto;
                })
                .collect(Collectors.toList());
   
            dto.setApprovers(approvers);
 
            // `createAuthorizationFromDto` 메서드 호출
            List<Long> referersList = new ArrayList<>();
            referersList.add(referer); // 참조자를 리스트로 변환

            // 생성된 DTO와 결재자, 참조자 정보를 전달하여 결재 생성
            Authorization savedAuthorization = authorizationService.createAuthorizationFromDto(dto, approvers, referersList);

            System.out.println("Saved Authorization: " + savedAuthorization); // 저장된 Authorization 확인

            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "문서가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_msg", "문서 저장 중 오류가 발생하였습니다.");
        }

        return resultMap;
    }

    /**
     * 결재 문서를 임시 저장하는 API 엔드포인트.
     * 
     * 이 메서드는 사용자가 제출한 결재 문서 정보를 포함하는 DTO를 받아
     * 문서 제목, 내용 및 타입 등을 설정하고, 결재자 및 참조자 정보를 제외하여
     * 임시 저장된 결재 문서를 데이터베이스에 저장합니다.
     * 
     * ## 기능
     * - 임시 저장할 결재 문서 정보 DTO를 생성하고 설정
     * - 기안자 정보를 기반으로 임시 결재 문서 생성
     * - 선택적 파일 첨부 기능 제공
     * - 결재자 및 참조자 정보를 제외하고 임시 저장
     * 
     * ## 기술
     * - Spring Boot 프레임워크의 REST API 방식으로 구현
     * - Jackson 라이브러리를 사용하여 JSON 데이터 처리
     * - MultipartFile을 통해 파일 업로드 처리
     * - Service 레이어를 통해 비즈니스 로직 수행
     * 
     * @param dto 결재 문서 정보를 담고 있는 DTO
     * @param file 첨부할 파일 (선택적)
     * @param title 문서 제목
     * @param docType 문서 타입
     * @param content 문서 내용
     * @param leaveType 휴가 유형 (선택적)
     * @param startDate 시작 날짜 (선택적)
     * @param endDate 종료 날짜 (선택적)
     * @param startEndDate 시작과 종료 날짜의 간격 (선택적)
     * @param lateType 지각 유형 (선택적)
     * @param tripType 출장 유형 (선택적)
     * @param outsideType 외출 유형 (선택적)
     * @param overtimeType 초과 근무 유형 (선택적)
     * @param memNo 기안자의 멤버 번호 (필수)
     * @return 문서 임시 저장 결과를 포함하는 맵
     */
    @ResponseBody
    @PostMapping("/authorization/saveTemp")
    public Map<String, String> saveTemporaryAuthorization(
            AuthorizationDto dto, 
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("docType") String docType,
            @RequestParam("content") String content,
            @RequestParam(value = "leaveType", required = false) String leaveType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "startEndDate", required = false) Double startEndDate,
            @RequestParam(value = "lateType", required = false) String lateType,
            @RequestParam(value = "tripType", required = false) String tripType,
            @RequestParam(value = "outsideType", required = false) String outsideType,
            @RequestParam(value = "overtimeType", required = false) String overtimeType,
            @RequestParam(value = "memberNo", required = true) Long memNo) {

        System.out.println("Received DTO: " + dto);
        System.out.println("Received Title: " + title);
        System.out.println("Received DocType: " + docType);

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "임시 저장 중 오류가 발생하였습니다");

        try {
            // memberNo 설정
            dto.setMemNo(memNo);

            // Member 정보를 조회하여 memName, empNo, distributorNo 설정
            Optional<Member> memberOpt = memberRepository.findByMemNo(memNo);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                dto.setMemName(member.getMemName()); // 기안자 이름 설정
                dto.setEmpNo(member.getEmpNo()); // 사번 설정
                dto.setDistributorNo(member.getDistributor().getDistributorNo()); // 소속 설정
            } else {
                resultMap.put("res_msg", "해당 멤버를 찾을 수 없습니다.");
                return resultMap;
            }

            // 제목 설정
            dto.setAuthTitle(title);
            dto.setAuthContent(content);  // content 값을 authContent에 설정

            // 문서 타입 설정
            dto.setDoctype(docType);
            dto.setAuthorName(docType);  // 문서 타입을 작성자 이름에 임시 저장
            dto.setAuthorStatus("T");  // 임시 저장 상태

            // 문서 타입에 따라 DTO 설정
            switch (docType) {
                case "off Report":
                    dto.setLeaveType(leaveType);
                    dto.setStartDate(startDate);
                    dto.setEndDate(endDate);
                    dto.setStartEndDate(startEndDate);
                    break;
                case "late Report":
                    dto.setLateType(lateType);
                    dto.setStartDate(startDate);
                    break;
                case "trip Report":
                    dto.setTripType(tripType);
                    dto.setStartDate(startDate);
                    dto.setEndDate(endDate);
                    break;
                case "outside Report":
                    dto.setOutsideType(outsideType);
                    dto.setStartDate(startDate);
                    dto.setEndDate(endDate);
                    break;
                case "overtime Report":
                    dto.setOvertimeType(overtimeType);
                    dto.setStartDate(startDate);
                    dto.setEndDate(endDate);
                    dto.setStartEndDate(startEndDate);
                    break;
                default:
                    throw new IllegalArgumentException("알 수 없는 문서 유형입니다: " + docType);
            }

            // 파일 업로드 처리
            if (file != null && !file.isEmpty()) {
                String savedFileName = authorizationFileService.upload(file);
                if (savedFileName != null) {
                    dto.setAuthorOriThumbnail(file.getOriginalFilename());
                    dto.setAuthorNewThumbnail(savedFileName);
                } else {
                    resultMap.put("res_msg", "파일 업로드 실패");
                    return resultMap;
                }
            }

            // 결재자 및 참조자 제외하여 저장
            // 이 부분이 결재자와 참조자 정보를 제외하는 부분입니다.
            dto.setApprovers(new ArrayList<>());  // 빈 리스트로 설정하여 결재자 정보 제외
            dto.setReferers(new ArrayList<>());   // 빈 리스트로 설정하여 참조자 정보 제외

            // `createAuthorizationFromDto` 메서드 호출 없이 DTO를 임시 저장
            Authorization tempAuthorization = authorizationService.saveTemporaryAuthorization(dto);

            System.out.println("Saved Temporary Authorization: " + tempAuthorization); // 저장된 임시 Authorization 확인

            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "문서가 임시 저장되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_msg", "임시 저장 중 오류가 발생하였습니다.");
        }

        return resultMap;
    }

    /**
     * 결재 모달창에서 결재 상세 정보를 조회하는 API 엔드포인트.
     * 
     * 주어진 결재 번호(authorNo)에 해당하는 결재 정보를 데이터베이스에서 조회하고,
     * 해당 결재와 관련된 결재자 및 참조자 정보를 포함한 DTO를 반환합니다.
     * 
     * ## 기능
     * - 결재 번호에 해당하는 결재 정보를 조회
     * - 해당 결재에 대한 결재자 및 참조자 목록을 로드
     * - 결재자 및 참조자의 서명을 포함한 DTO를 생성
     * 
     * ## 기술
     * - Spring MVC를 사용한 RESTful API 구현
     * - JPA를 통해 데이터베이스와의 상호작용 수행
     * - DTO를 사용하여 클라이언트에 필요한 데이터 형식으로 변환
     * 
     * @param authorNo 결재 번호 (필수)
     * @return 결재 정보를 포함하는 AuthorizationDto 객체 또는 null
     */
    @RequestMapping(value = "/api/authorization/{authorNo}", method = RequestMethod.GET)
    @ResponseBody
    public AuthorizationDto getAuthorizationById(@PathVariable("authorNo") Long authorNo) {
        System.out.println("Entering getAuthorizationById method with authorNo: " + authorNo);
        
        if (authorNo == null) {
            System.out.println("authorNo is null");
        } else {
            System.out.println("authorNo is not null: " + authorNo);
        }
        
        // Authorization 엔터티 로드
        Authorization authorization = authorizationService.getAuthorizationById(authorNo);
        
        if (authorization != null) {
            System.out.println("Found authorization: " + authorization);

            // approvalRoute 테이블에서 결재자 및 참조자 리스트 가져오기
            List<ApprovalRoute> approvalRoutes = approvalRouteRepository.findByAuthorization_AuthorNo(authorNo);

            // 결재자와 참조자를 구분하여 리스트에 추가
            List<ApprovalRouteDto> approvers = new ArrayList<>();
            List<ApprovalRouteDto> referers = new ArrayList<>();
            
            for (ApprovalRoute route : approvalRoutes) {
                ApprovalRouteDto dto = ApprovalRouteDto.toDto(route);
                
                // 서명 필드 추가
                dto.setApproverSignature(route.getApproverSignature());
                dto.setRefererSignature(route.getRefererSignature());

                // 결재자인 경우 리스트에 추가
                if ("Y".equals(route.getIsApprover())) {
                    approvers.add(dto);
                }

                // 참조자인 경우 리스트에 추가
                if ("Y".equals(route.getIsReferer())) {
                    referers.add(dto);
                }
            }
            
            // AuthorizationDto로 변환 후 결재자와 참조자 리스트 추가
            AuthorizationDto authorizationDto = AuthorizationDto.toDto(authorization);
            authorizationDto.setApprovers(approvers);
            authorizationDto.setReferers(referers);
            
            System.out.println("Approvers: " + approvers.size());
            System.out.println("Referers: " + referers.size());
            
            return authorizationDto;
        } else {
            System.out.println("No authorization found for authorNo: " + authorNo);
            return null;
        }
    }
    
    /**
     * 결재 확인(authorizationCheck) 모달창에서 결재 문서에 대한 승인 또는 반려 처리를 수행하는 API 엔드포인트.
     * 
     * ## 기능
     * - 로그인한 사용자의 정보를 기반으로 결재 문서의 승인 또는 반려를 처리
     * - 결재자의 서명을 저장하고, 결재 경로 상태를 업데이트
     * - 모든 결재자와 참조자의 상태를 확인하여 문서 상태를 업데이트
     * 
     * ## 기술
     * - Spring MVC를 사용한 RESTful API 구현
     * - Spring Security를 통해 사용자 인증 및 권한 관리
     * - JPA를 통해 데이터베이스와의 상호작용 수행
     * 
     * @param authorNo 결재 번호 (필수)
     * @param signature 결재자의 서명 (필수)
     * @param action "approve" 또는 "reject" (필수) - 승인 또는 반려를 나타냄
     * @return 처리 결과 메시지
     */
	@PostMapping("/api/authorization/sign")
	public ResponseEntity<String> signAuthorization(@RequestParam("authorNo") Long authorNo,
	                                                @RequestParam("signature") String signature,
	                                                @RequestParam("action") String action) {
	    // 로그인한 사용자의 memNo 가져오기
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
	        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
	        Long memNo = securityUser.getMember().getMemNo(); // 로그인한 사용자의 memNo

	        if (action.equals("approve")) {
	            try {
	                // 승인 요청 처리
	                authorizationService.approveDocument(authorNo, signature,memNo);

	                // 승인 시 결재자의 서명을 저장
	                authorizationService.updateApproverSignature(authorNo, memNo, signature);

	            } catch (IllegalArgumentException e) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	            }
	        } else if (action.equals("reject")) {
	            // 반려 요청 처리
	            authorizationService.rejectDocument(authorNo, signature);

	            // 반려 시 참조자의 서명을 저장
	            authorizationService.updateRefererSignature(authorNo, memNo, signature);
	        }

	        // 결재 경로 상태 업데이트 (승인/반려)
	        authorizationService.updateApprovalRouteStatus(authorNo, action);

	        // 모든 결재자와 참조자의 상태 확인 후 문서 상태 업데이트
	        authorizationService.checkAndUpdateDocumentStatus(authorNo);

	        return ResponseEntity.ok("처리가 완료되었습니다.");
	    } else {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인된 사용자가 없습니다.");
	    }
	}

	/**
	 * Spring MVC의 데이터 바인딩을 위한 초기 설정 메서드.
	 * 
	 * ## 기능
	 * - 요청 파라미터를 바인딩할 때 사용되는 커스텀 에디터를 등록
	 * - JSON 형식의 문자열을 List<ApprovalRouteDto> 객체로 변환하여 바인딩
	 * 
	 * ## 기술
	 * - `WebDataBinder`를 사용하여 특정 필드에 대한 커스텀 변환 로직을 정의
	 * - Jackson의 `ObjectMapper`를 사용하여 JSON 문자열을 객체로 변환
	 * 
	 * @param binder WebDataBinder 객체 - 요청 파라미터와 자바 객체 간의 변환을 처리
	 */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        ObjectMapper objectMapper = new ObjectMapper();
        binder.registerCustomEditor(List.class, "approvers", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                try {
                    // approvers 필드를 List<ApprovalRouteDto>로 변환
                    List<ApprovalRouteDto> approvers = Arrays.asList(objectMapper.readValue(text, ApprovalRouteDto[].class));
                    setValue(approvers);
                } catch (Exception e) {
                    setValue(null);
                }
            }
        });
    }
	
    /**
     * 문서 회수를 위한 API 엔드포인트.
     * 
     * ## 기능
     * - 주어진 문서 번호(authorNo)를 사용하여 문서를 회수하는 서비스 호출
     * - 문서 번호가 유효한지 검증하고, 결과에 따라 적절한 HTTP 응답 반환
     * 
     * ## 기술
     * - POST 요청을 처리하고 JSON 형식의 요청 본문을 받음
     * - 문서 회수 작업 중 발생할 수 있는 예외를 처리
     * 
     * @param request 요청 본문에서 문서 번호를 포함한 Map
     * @return ResponseEntity - 문서 회수 결과에 대한 HTTP 응답
     */
    @PostMapping("/authorization/recall")
    public ResponseEntity<?> recallDocument(@RequestBody Map<String, Long> request) {
        Long authorNo = request.get("authorNo");
        System.out.println("authorNo: " + authorNo);  // 전달된 authorNo 값 출력

        // authorNo가 null인 경우 처리
        if (authorNo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("문서 번호가 없습니다.");
        }

        try {
            authorizationService.recallDocument(authorNo);  // 회수 서비스 호출
            return ResponseEntity.ok("문서가 성공적으로 회수되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("문서 회수 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자에게 알림을 가져오는 API 엔드포인트.
     * 
     * ## 기능
     * - 현재 로그인한 사용자의 멤버 ID를 기반으로 알림 정보를 조회
     * - 결재 알림과 작성자 알림의 상태를 확인하고 결과를 반환
     * 
     * ## 기술
     * - GET 요청을 처리하며, 현재 로그인한 사용자의 정보를 확인
     * - 알림의 상태를 Boolean 값으로 담은 맵을 반환
     * 
     * @return ResponseEntity<Map<String, Boolean>> - 알림 상태를 포함하는 HTTP 응답
     */
    @GetMapping("/nav")
    public ResponseEntity<Map<String, Boolean>> getNavNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memId = authentication.getName();
        Optional<Member> memberOpt = memberRepository.findByMemId(memId);

        Map<String, Boolean> notifications = new HashMap<>();

        if (memberOpt.isPresent()) {
            Long memNo = memberOpt.get().getMemNo();
            
            // 'C' 상태를 제외한 알림만 확인
            boolean approvalNotification = approvalRouteService.hasApprovalNotifications(memNo);
            boolean authorNotification = authorizationService.hasAuthorNotifications(memNo);

            notifications.put("approvalNotification", approvalNotification);
            notifications.put("authorNotification", authorNotification);
        }

        return ResponseEntity.ok(notifications);
    }

    /**
     * 특정 결재자의 알림을 제거하는 API 엔드포인트.
     * 
     * ## 기능
     * - 로그인한 사용자의 ID를 기반으로 특정 결재자의 알림을 삭제
     * 
     * ## 기술
     * - GET 요청을 처리하며, 결재자 번호와 로그인한 사용자 정보를 통해 알림 삭제
     * - 알림 삭제가 성공적으로 수행되면 HTTP 200 OK 응답을 반환
     * 
     * @param authorNo - 삭제할 결재자 알림의 번호
     * @param principal - 현재 인증된 사용자 정보
     * @return ResponseEntity<Void> - 삭제 결과에 따른 HTTP 응답
     */
    @GetMapping("/authorization/clearAuthorNotification/{authorNo}")
    public ResponseEntity<Void> clearAuthorNotification(@PathVariable("authorNo") Long authorNo, Principal principal) {
        String memId = principal.getName();
        Optional<Member> memberOpt = memberRepository.findByMemId(memId);

        if (memberOpt.isPresent()) {
            Long memNo = memberOpt.get().getMemNo();
            authorizationService.clearAuthorNotification(authorNo, memNo); // 알림 삭제 로직 실행
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 로그인된 사용자가 없는 경우
        }
    }


}
