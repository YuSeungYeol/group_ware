package com.ware.spring.authorization.service;

import java.io.File;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ware.spring.approval_route.domain.ApprovalRoute;
import com.ware.spring.approval_route.domain.ApprovalRouteDto;
import com.ware.spring.approval_route.repository.ApprovalRouteRepository;
import com.ware.spring.approval_route.service.ApprovalRouteService;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.domain.AuthorizationDto;
import com.ware.spring.authorization.repository.AuthorizationRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.security.vo.SecurityUser;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.core.Authentication;


@Service
public class AuthorizationService {

    private final AuthorizationRepository authorizationRepository;
    private final ApprovalRouteService approvalRouteService;
    private final MemberRepository memberRepository;
    private final ApprovalRouteRepository approvalRouteRepository;

    @Autowired
    public AuthorizationService(AuthorizationRepository authorizationRepository, ApprovalRouteService approvalRouteService, 
                                MemberRepository memberRepository, ApprovalRouteRepository approvalRouteRepository) {
        this.authorizationRepository = authorizationRepository;
        this.approvalRouteService = approvalRouteService;
        this.memberRepository = memberRepository;
        this.approvalRouteRepository = approvalRouteRepository;
    }

    // Authorization 목록 조회
    public List<AuthorizationDto> selectAuthorizationList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            Long memNo = securityUser.getMember().getMemNo();  // 로그인한 사용자의 memNo 가져오기

            // 본인 결재 리스트 중 임시 저장(T) 상태가 아닌 문서만 가져오기
            List<Authorization> authorizationList = authorizationRepository.findByMember_MemNo(memNo)
                    .stream()
                    .filter(authorization -> !"T".equals(authorization.getAuthorStatus()))  // 임시 저장 제외
                    .toList();

            // DTO로 변환 후 반환
            return authorizationList.stream()
                .map(AuthorizationDto::toDto)
                .toList();
        }
        return Collections.emptyList(); // 인증되지 않은 경우 빈 리스트 반환
    }

    // Authorization 엔티티 직접 생성
    @Transactional
    public Authorization createAuthorization(Authorization authorization) {
        return authorizationRepository.save(authorization);
    }

    // 결재
    @Transactional
    public Authorization createAuthorizationFromDto(AuthorizationDto dto, List<ApprovalRouteDto> approvers, List<Long> referers) {
        // 시작 로그
        System.out.println("Starting createAuthorizationFromDto with DTO: " + dto);
        System.out.println("Approvers: " + approvers);
        System.out.println("Referers: " + referers);
        
        // SecurityContext에서 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            Member loggedInMember = securityUser.getMember();  // 로그인된 사용자의 Member 정보 가져오기
            dto.setMemNo(loggedInMember.getMemNo());  // DTO에 로그인한 사용자의 memNo 설정
        } else {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        // Member 조회 시 Optional 처리
        Optional<Member> optionalMember = memberRepository.findByMemNo(dto.getMemNo());
        Member member = optionalMember.orElseThrow(() -> new IllegalArgumentException("Invalid Member ID: " + dto.getMemNo()));

        // Authorization 엔티티 생성 (authorNo는 null이어야 함)
        Authorization authorization = dto.toEntity(member, new ArrayList<>());
        System.out.println("Authorization Entity before save: " + authorization); // 저장 전 Entity 확인

        // Authorization에 authContent 값을 설정
        if (dto.getAuthContent() != null && !dto.getAuthContent().isEmpty()) {
            authorization.setAuthContent(dto.getAuthContent());  // authContent 값 설정
        } else {
            System.out.println("Authorization Content is empty or null");  // 로그 출력
        }

        // Authorization 저장 전 확인
        System.out.println("Saving Authorization Entity: " + authorization);
        // Authorization 저장 및 즉시 반영
        Authorization savedAuthorization = authorizationRepository.saveAndFlush(authorization);  // saveAndFlush로 변경
        System.out.println("Saved Authorization Entity: " + savedAuthorization);  // 저장 후 확인

        // DTO에 저장된 authorNo 설정
        dto.setAuthorNo(savedAuthorization.getAuthorNo());
        System.out.println("After saving, generated AuthorNo is: " + savedAuthorization.getAuthorNo());  // 로그 추가
        
        // 결재자 및 참조자 목록에서 중복 제거
        Set<Long> uniqueApprovers = approvers.stream()
                                             .map(ApprovalRouteDto::getMemNo)
                                             .collect(Collectors.toSet());
        Set<Long> uniqueReferers = new HashSet<>(referers);    // 참조자 목록의 중복 제거

        // 결재 경로 생성 전 확인
        System.out.println("Creating Approval Routes for Authorization No: " + savedAuthorization.getAuthorNo());
        approvalRouteService.createApprovalRoutesForDocument(savedAuthorization.getAuthorNo(), new ArrayList<>(uniqueApprovers), new ArrayList<>(uniqueReferers));
        System.out.println("Approval Routes created successfully.");

        return savedAuthorization;
    }


    // 결재자 경로 업데이트
    @Transactional
    public void saveAuthorizationData(AuthorizationDto dto, List<Long> approvers, Long referer) {
        int order = 1;

        for (Long approver : approvers) {
            if (!approvalRouteService.getCurrentApprover(dto.getAuthorNo(), order).isPresent()) {
                approvalRouteService.createApprovalRoute(dto.getAuthorNo(), approver, "P", order++, true, false,null);
            }
        }

        if (referer != null) {
            approvalRouteService.createApprovalRoute(dto.getAuthorNo(), referer, "P", order, false, true,null);
        }
    }

    // 파일 삭제 메서드
    public void cleanUploadFile(String rename, String path) {
        for (String s : rename.split(rename)) {
            File f = new File(path, s);
            f.delete();
        }
    }
    // 임시 저장함
    public Page<AuthorizationDto> selectTemporaryAuthorizationList(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            Long memNo = securityUser.getMember().getMemNo();  // 로그인한 사용자의 memNo 가져오기

            // 현재 사용자의 임시 저장 문서만 가져오기, 페이징 처리
            Page<Authorization> authorizationPage = authorizationRepository.findByAuthorStatusAndMember_MemNo("T", memNo, pageable);

            // Page<Authorization>을 Page<AuthorizationDto>로 변환하여 반환
            return authorizationPage.map(AuthorizationDto::toDto);
        }
        return Page.empty(); // 인증되지 않은 경우 빈 페이지 반환
    }




    
    
    // 임시 저장
    @Transactional
    public Authorization saveTemporaryAuthorization(AuthorizationDto dto) {
        // SecurityContext에서 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            Member loggedInMember = securityUser.getMember();  // 로그인된 사용자의 Member 정보 가져오기
            dto.setMemNo(loggedInMember.getMemNo());  // DTO에 로그인한 사용자의 memNo 설정
        } else {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        // Member 조회 시 Optional 처리
        Optional<Member> optionalMember = memberRepository.findByMemNo(dto.getMemNo());
        Member member = optionalMember.orElseThrow(() -> new IllegalArgumentException("Invalid Member ID: " + dto.getMemNo()));

        // Authorization 엔티티 생성
        Authorization authorization = dto.toEntity(member, new ArrayList<>());
        
        // Authorization에 authContent 값을 설정
        if (dto.getAuthContent() != null && !dto.getAuthContent().isEmpty()) {
            authorization.setAuthContent(dto.getAuthContent());  // authContent 값 설정
        } else {
            System.out.println("Authorization Content is empty or null");  // 로그 출력
        }
        
        // 임시 저장 상태 설정
        authorization.setAuthorStatus("T");  // 임시 저장 상태로 설정

        // 엔티티를 저장 (임시 저장)
        Authorization savedAuthorization = authorizationRepository.save(authorization);  // 저장 후 반환된 엔티티 객체를 저장

        return savedAuthorization;  // 저장된 Authorization 객체 반환
    }


    // 임시 저장한 파일 다시 작성
    public Authorization getAuthorizationById(Long authorNo) {
        System.out.println("Looking for Authorization with authorNo: " + authorNo); // 디버깅용 로그
        
        Authorization authorization = authorizationRepository.findById(authorNo)
            .orElseThrow(() -> new IllegalArgumentException("Authorization not found: " + authorNo));
        
        // Authorization 객체에 연관된 Member와 Distributor 정보를 출력하여 확인
        System.out.println("Member Name: " + (authorization.getMember() != null ? authorization.getMember().getMemName() : "null"));
        System.out.println("Distributor No: " + (authorization.getMember() != null && authorization.getMember().getDistributor() != null ? authorization.getMember().getDistributor().getDistributorNo() : "null"));
        
        return authorization;
    }

    // 결재 확인 관련 결재자, 참조자 승인 확인란
    public Page<AuthorizationDto> selectAuthorizationListForApproversAndReferers(String memId, Pageable pageable) {
        System.out.println("로그인한 사용자 ID: " + memId);
        Optional<Member> currentMember = memberRepository.findByMemId(memId);
        System.out.println("현재 로그인한 사용자 memNo: " + (currentMember.isPresent() ? currentMember.get().getMemNo() : "없음"));

        if (currentMember.isPresent()) {
            Long memNo = currentMember.get().getMemNo();
            // 페이징 처리된 결재 경로 가져오기
            Page<ApprovalRoute> approvalRoutesPage = approvalRouteRepository.findByMember_MemNo(memNo, pageable);

            // DTO로 변환
            return approvalRoutesPage.map(approvalRoute -> {
                Authorization authorization = approvalRoute.getAuthorization();
                AuthorizationDto dto = AuthorizationDto.toDto(authorization);

                dto.setIsApprover(approvalRoute.getIsApprover());
                dto.setIsReferer(approvalRoute.getIsReferer());

                // ApprovalRouteDto로 변환하여 설정
                List<ApprovalRouteDto> routeDtos = approvalRouteRepository.findByAuthorization_AuthorNo(authorization.getAuthorNo())
                    .stream()
                    .map(route -> {
                        ApprovalRouteDto routeDto = ApprovalRouteDto.toDto(route);

                        // 결재자 서명 추가
                        if ("Y".equals(route.getIsApprover())) {
                            routeDto.setApproverSignature(route.getApproverSignature());
                        }

                        // 참조자 서명 추가
                        if ("Y".equals(route.getIsReferer())) {
                            routeDto.setRefererSignature(route.getRefererSignature());
                        }

                        return routeDto;
                    })
                    .collect(Collectors.toList());

                dto.setApprovalRoutes(routeDtos); // approvalRoute 목록을 DTO에 설정
                return dto;
            });
        }

        System.out.println("로그인된 사용자 정보 없음");
        return Page.empty(); // 인증되지 않은 경우 빈 페이지 반환
    }
    
   


    // 승인 처리 메서드
    @Transactional
    public void approveDocument(Long authorNo, String signature, Long memNo) {
        Authorization authorization = authorizationRepository.findById(authorNo)
            .orElseThrow(() -> new IllegalArgumentException("해당 문서를 찾을 수 없습니다."));

        // 현재 결재 경로에서 해당 멤버(memNo)를 찾음
        Optional<ApprovalRoute> approvalRouteOpt = approvalRouteRepository.findByAuthorization_AuthorNoAndMember_MemNo(authorNo, memNo);
        
        if (!approvalRouteOpt.isPresent()) {
            throw new IllegalArgumentException("해당 결재 경로를 찾을 수 없습니다.");
        }

        ApprovalRoute approvalRoute = approvalRouteOpt.get();

        // 결재자인 경우 서명을 저장하고 승인 상태를 변경
        if ("Y".equals(approvalRoute.getIsApprover())) {
            approvalRoute.setApproverSignature(signature);  // 결재자 서명 저장
            approvalRoute.setApprovalStatus("Y");  // 결재자 승인 상태로 변경
            approvalRouteRepository.save(approvalRoute);  // 결재 경로 저장
        }

        // 참조자인 경우 서명을 저장하고 승인 상태를 변경
        if ("Y".equals(approvalRoute.getIsReferer())) {
            approvalRoute.setRefererSignature(signature);  // 참조자 서명 저장
            approvalRoute.setApprovalStatus("Y");  // 참조자 승인 상태로 변경
            approvalRouteRepository.save(approvalRoute);  // 참조 경로 저장
        }

        // 문서 상태 변경 로직 (필요한 경우 추가 로직)
        if ("off Report".equals(authorization.getDoctype())) {
            // 모든 결재자가 승인했는지 확인
            boolean allApproversApproved = checkAllApproversApproved(authorNo);

            if (allApproversApproved) {
                Member member = authorization.getMember(); // 기안자의 멤버 정보 가져옴
                Double startEndDate = authorization.getStartEndDate(); // 사용하려는 연차 일수
                Double memOff = member.getMemOff(); // 남아 있는 연차 일수
                Double memUseOff = member.getMemUseOff(); // 사용한 연차 일수

                // 남아 있는 연차가 부족한 경우
                if (memOff < startEndDate) {
                    authorization.setAuthorStatus("N");
                    authorizationRepository.save(authorization);
                    throw new IllegalArgumentException("남아 있는 연차 일수가 부족합니다.");
                }

                // 남아 있는 연차에서 사용한 연차 차감
                member.setMemOff(memOff - startEndDate);
                member.setMemUseOff(memUseOff + startEndDate);
                memberRepository.save(member);
            }
        }

        // 결재자 또는 참조자 상태가 'Y'로 변경되었으면, 문서 상태도 승인 상태로 변경
        authorization.setAuthorStatus("Y"); 
        authorizationRepository.save(authorization);
    }


    
    // 모든 결재자가 승인했는지 확인하는 메서드
    public boolean checkAllApproversApproved(Long authorNo) {
        List<ApprovalRoute> approvalRoutes = approvalRouteRepository.findByAuthorization_AuthorNo(authorNo);

        // 결재자 수 확인
        long totalApprovers = approvalRoutes.stream()
            .filter(route -> "Y".equals(route.getIsApprover())) // 결재자만 필터링
            .count();

        // 승인된 결재자 수 확인
        long approvedCount = approvalRoutes.stream()
            .filter(route -> "Y".equals(route.getIsApprover())) // 결재자만 필터링
            .filter(route -> "Y".equals(route.getApprovalStatus())) // 승인된 결재자만 필터링
            .count();

        // 모든 결재자가 승인했는지 확인
        return totalApprovers == approvedCount;
    }



	
	    // 반려 처리 메서드
	    public void rejectDocument(Long authorNo, String signature) {
	        Authorization authorization = authorizationRepository.findById(authorNo)
	            .orElseThrow(() -> new IllegalArgumentException("해당 문서를 찾을 수 없습니다."));
	        
	        authorization.setSignature(signature);
	        authorization.setAuthorStatus("N"); // 반려 상태로 변경
	        authorizationRepository.save(authorization);
	    }
	
	    // 모든 결재자와 참조자의 상태를 확인 후 문서 상태를 업데이트
	    @Transactional
	    public void checkAndUpdateDocumentStatus(Long authorNo) {
	        List<ApprovalRoute> routes = approvalRouteRepository.findByAuthorization_AuthorNo(authorNo);

	        // 모든 결재자와 참조자가 승인했는지 확인
	        boolean allApproved = routes.stream()
	            .filter(route -> "Y".equals(route.getIsApprover()) || "Y".equals(route.getIsReferer()))
	            .allMatch(route -> "Y".equals(route.getApprovalStatus())); // 모든 결재자와 참조자가 'Y'인지 확인

	        // 결재자 또는 참조자 중 하나라도 반려했는지 확인
	        boolean anyRejected = routes.stream()
	            .filter(route -> "Y".equals(route.getIsApprover()) || "Y".equals(route.getIsReferer()))
	            .anyMatch(route -> "N".equals(route.getApprovalStatus())); // 'N'인 경로가 있는지 확인

	        Authorization authorization = authorizationRepository.findById(authorNo)
	            .orElseThrow(() -> new IllegalArgumentException("해당 문서를 찾을 수 없습니다."));

	        // 모든 결재자와 참조자가 승인한 경우
	        if (allApproved) {
	            authorization.setAuthorStatus("Y"); // 전체 승인
	        }
	        // 결재자 또는 참조자 중 하나라도 반려한 경우
	        else if (anyRejected) {
	            authorization.setAuthorStatus("N"); // 하나라도 반려시 전체 반려
	        }
	        // 승인/반려가 결정되지 않은 경우는 상태 변경하지 않음 (기존 상태 유지)
	        else {
	            return; // 상태를 변경하지 않고 종료
	        }

	        // 변경된 상태 저장
	        authorizationRepository.save(authorization);
	    }



	    // 결재 경로의 상태 업데이트 (승인/반려)
	    @Transactional
	    public void updateApprovalRouteStatus(Long authorNo, String action) {
	        List<ApprovalRoute> routes = approvalRouteRepository.findByAuthorization_AuthorNo(authorNo);
	        
	        boolean previousApproved = true; // 이전 결재자가 승인되었는지 확인하는 변수
	        
	        for (ApprovalRoute route : routes) {
	            if ("P".equals(route.getApprovalStatus())) { // 대기중인 상태만 처리
	                if (previousApproved) {
	                    // 현재 결재자 상태 업데이트
	                    System.out.println("결재자가 승인됨: " + route.getMember().getMemNo());
	                    route.setApprovalStatus(action.equals("approve") ? "Y" : "N");
	                    route.setApprovedDate(LocalDateTime.now());
	                    approvalRouteRepository.save(route);
	                    previousApproved = action.equals("approve");
	                } else {
	                    System.out.println("이전 결재자가 반려됨: " + route.getMember().getMemNo());
	                    route.setApprovalStatus("N"); // 남은 결재자들은 반려 상태로 설정
	                    approvalRouteRepository.save(route);
	                }
	            }
	        }
	    }

	    // 완료된 문서 가져오기
	    public List<Authorization> findAllByAuthorStatus(List<String> statuses) {
	        return authorizationRepository.findAllByAuthorStatusIn(statuses);
	    }

	    // 기안 진행 중 목록 조회
	    public List<AuthorizationDto> selectDraftAuthorizationList() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
	            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
	            Long memNo = securityUser.getMember().getMemNo();  // 로그인한 사용자의 memNo 가져오기

	            // 본인 결재 리스트 중 대기(P) 상태인 문서만 가져오기
	            List<Authorization> draftAuthorizationList = authorizationRepository.findByMember_MemNo(memNo)
	                    .stream()
	                    .filter(authorization -> "P".equals(authorization.getAuthorStatus()))  // 'P' 상태만 필터링
	                    .toList();

	            // DTO로 변환 후 반환
	            return draftAuthorizationList.stream()
	                .map(AuthorizationDto::toDto)
	                .toList();
	        }
	        return Collections.emptyList(); // 인증되지 않은 경우 빈 리스트 반환
	    }
	    
	    // 완료된 문서 목록 조회
	    public List<AuthorizationDto> selectCompletedAuthorizationList() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
	            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
	            Long memNo = securityUser.getMember().getMemNo();  // 로그인한 사용자의 memNo 가져오기

	            // 승인(Y) 또는 반려(N) 상태인 문서만 가져오기
	            List<String> statuses = Arrays.asList("Y", "N", "C"); // 승인, 반려 상태 필터링
	            List<Authorization> completedAuthorizationList = authorizationRepository.findByMember_MemNoAndAuthorStatusIn(memNo, statuses);

	            System.out.println("Completed Authorization List: " + completedAuthorizationList);

	            
	            // DTO로 변환 및 결재 경로 추가
	            return completedAuthorizationList.stream()
	                .map(authorization -> {
	                    AuthorizationDto dto = AuthorizationDto.toDto(authorization);

	                    // 결재 경로 추가
	                    List<ApprovalRouteDto> approvalRouteDtos = approvalRouteRepository.findByAuthorization_AuthorNo(authorization.getAuthorNo())
	                            .stream()
	                            .map(route -> ApprovalRouteDto.toDto(route)) // ApprovalRoute -> ApprovalRouteDto 변환
	                            .collect(Collectors.toList());

	                    dto.setApprovalRoutes(approvalRouteDtos); // DTO에 결재 경로 추가
	                    return dto;
	                })
	                .collect(Collectors.toList()); // 결과 리스트 반환
	        }
	        return Collections.emptyList(); // 인증되지 않은 경우 빈 리스트 반환
	        
	    }

	    // 결재 경로를 회수된 상태로 업데이트하는 메서드
	    @Transactional
	    public void updateApprovalRouteStatusToRecalled(Long authorNo) {
	        List<ApprovalRoute> routes = approvalRouteRepository.findByAuthorization_AuthorNo(authorNo);
	        
	        for (ApprovalRoute route : routes) {
	            route.setApprovalStatus("R"); // 결재 경로 상태를 '회수됨(R)'으로 변경
	            approvalRouteRepository.save(route);
	        }
	    }
	    
	    // 문서 회수 로직
	    public void recallDocument(Long authorNo) {
	        Authorization authorization = authorizationRepository.findById(authorNo)
	            .orElseThrow(() -> new IllegalArgumentException("해당 문서를 찾을 수 없습니다."));

	        // 문서가 '대기중(P)' 상태인 경우만 회수 가능
	        if (!"P".equals(authorization.getAuthorStatus())) {
	            throw new IllegalArgumentException("대기중 상태가 아닌 문서는 회수할 수 없습니다.");
	        }

	        // 문서 상태를 'R'로 변경
	        authorization.setAuthorStatus("R");
	        authorizationRepository.save(authorization);

	        // 결재 경로도 'R'로 변경
	        approvalRouteService.updateApprovalRouteToRecalled(authorNo);
	        
	        
	    }

	    @Transactional
	    public void updateApproverSignature(Long authorNo, Long memNo, String signature) {
	        Optional<ApprovalRoute> optionalRoute = approvalRouteRepository.findByAuthorization_AuthorNoAndMember_MemNo(authorNo, memNo);

	        if (optionalRoute.isPresent()) {
	            ApprovalRoute approvalRoute = optionalRoute.get();
	            // 결재자 서명 업데이트
	            approvalRoute.setApproverSignature(signature); // `ApproverSignature` 필드에 서명 저장
	            approvalRouteRepository.save(approvalRoute);
	        } else {
	            throw new IllegalArgumentException("결재 경로를 찾을 수 없습니다.");
	        }
	    }
	    
	    @Transactional
	    public void updateRefererSignature(Long authorNo, Long memNo, String signature) {
	        Optional<ApprovalRoute> optionalRoute = approvalRouteRepository.findByAuthorization_AuthorNoAndMember_MemNo(authorNo, memNo);

	        if (optionalRoute.isPresent()) {
	            ApprovalRoute approvalRoute = optionalRoute.get();
	            // 참조자 서명 업데이트
	            approvalRoute.setRefererSignature(signature); // `RefererSignature` 필드에 서명 저장
	            approvalRouteRepository.save(approvalRoute);
	        } else {
	            throw new IllegalArgumentException("참조자 경로를 찾을 수 없습니다.");
	        }
	    }
	    
	    // 기안 진행 목록 페이징 처리
	    public Page<Authorization> getDraftAuthorizations(Pageable pageable) {
	        return authorizationRepository.findByAuthorStatus("P", pageable); // "P"는 진행 중인 상태
	    }

	    // 완료된 문서 목록 페이징 처리
	    public Page<Authorization> getCompletedAuthorizations(Pageable pageable) {
	        return authorizationRepository.findByAuthorStatusIn(Arrays.asList("Y", "N", "C"), pageable); // "Y": 승인, "N": 반려
	    }


	    // 기안자 알람
	    public boolean hasAuthorNotifications(Long memNo) {
	        return authorizationRepository.existsByMember_MemNoAndAuthorStatusNotAndAuthorStatusNot(memNo, "C", "T");
	    }
	    
	    // 알림 해제 로직
	    @Transactional
	    public void clearAuthorNotification(Long authorNo, Long memNo) {
	        System.out.println("clearAuthorNotification 메서드 호출됨, authorNo: " + authorNo + ", memNo: " + memNo);
	        
	        // 문서가 memNo 사용자의 문서인지 확인
	        Optional<Authorization> authorizationOpt = authorizationRepository.findByAuthorNoAndMember_MemNo(authorNo, memNo);
	        System.out.println("authorizationOpt.isPresent(): " + authorizationOpt.isPresent());

	        if (authorizationOpt.isPresent()) {
	            Authorization authorization = authorizationOpt.get();
	            System.out.println("현재 문서 상태: " + authorization.getAuthorStatus());

	            // 'Y'(승인) 또는 'N'(반려) 상태일 때만 상태를 'C'로 변경하여 알림을 제거
	            if ("Y".equals(authorization.getAuthorStatus()) || "N".equals(authorization.getAuthorStatus()) || "R".equals(authorization.getAuthorStatus())) {
	                authorization.setAuthorStatus("C"); // 상태를 'C'로 변경
	                authorizationRepository.save(authorization); // 상태 업데이트
	                System.out.println("상태가 'C'로 변경되었습니다. 문서 번호: " + authorNo);
	            } else {
	                System.out.println("알림 상태를 'C'로 변경할 수 없습니다. 현재 상태: " + authorization.getAuthorStatus());
	            }
	        } else {
	            System.out.println("Authorization을 찾을 수 없습니다. 문서 번호: " + authorNo);
	        }
	    }


	 // 기안 진행 중인 본인의 문서만 가져오기
	    @Transactional
	    public Page<AuthorizationDto> getDraftAuthorizations(Long memNo, Pageable pageable) {
	        Page<Authorization> authorizationPage = authorizationRepository.findByAuthorStatusAndMember_MemNo("P", memNo, pageable);
	        return authorizationPage.map(AuthorizationDto::toDto);
	    }

	    // 완료된 본인의 문서만 가져오기
	    @Transactional
	    public Page<AuthorizationDto> getCompletedAuthorizations(Long memNo, Pageable pageable) {
	        List<String> completedStatuses = Arrays.asList("Y", "N", "C");
	        Page<Authorization> authorizationPage = authorizationRepository.findByAuthorStatusInAndMember_MemNo(completedStatuses, memNo, pageable);
	        return authorizationPage.map(AuthorizationDto::toDto);
	    }





}
