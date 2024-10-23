package com.ware.spring.approval_route.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ware.spring.approval_route.domain.ApprovalRoute;
import com.ware.spring.approval_route.domain.ApprovalRouteDto;
import com.ware.spring.approval_route.repository.ApprovalRouteRepository;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.repository.AuthorizationRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApprovalRouteService {

    private final ApprovalRouteRepository approvalRouteRepository;
    private final AuthorizationRepository authorizationRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public ApprovalRouteService(ApprovalRouteRepository approvalRouteRepository, AuthorizationRepository authorizationRepository, MemberRepository memberRepository) {
        this.approvalRouteRepository = approvalRouteRepository;
        this.authorizationRepository = authorizationRepository;
        this.memberRepository = memberRepository;
    }

    // 특정 authorNo에 대한 결재자와 참조자 상태 조회 (모든 상태의 문서 포함)
    public List<ApprovalRouteDto> getApprovalRoutesByAuthorNo(Long authorNo) {
        List<ApprovalRoute> approvalRoutes = approvalRouteRepository.findByAuthorization_AuthorNo(authorNo);

        return approvalRoutes.stream()
            .map(approvalRoute -> {
                Member approver = null;
                Member referer = null;
                String approverSignature = null;
                String refererSignature = null;

                // 결재자가 존재하는 경우
                if ("Y".equals(approvalRoute.getIsApprover())) {
                    approver = memberRepository.findById(approvalRoute.getMember().getMemNo()).orElse(null);
                    if (approver != null) {
                        approverSignature = approvalRoute.getApproverSignature(); // Approver의 서명 정보 가져오기
                    }
                }

                // 참조자가 존재하는 경우
                if ("Y".equals(approvalRoute.getIsReferer())) {
                    referer = memberRepository.findById(approvalRoute.getMember().getMemNo()).orElse(null);
                    if (referer != null) {
                        refererSignature = approvalRoute.getRefererSignature(); // Referer의 서명 정보 가져오기
                    }
                }

                // Dto로 변환
                ApprovalRouteDto dto = ApprovalRouteDto.toDto(approvalRoute, approver, referer);
                dto.setApproverSignature(approverSignature);  // 결재자의 서명 정보 추가
                dto.setRefererSignature(refererSignature);    // 참조자의 서명 정보 추가
                return dto;
            })
            .collect(Collectors.toList());
    }




    // 특정 authorNo와 memberNo에 대한 ApprovalRoute 상태 업데이트
    @Transactional
    public void updateApprovalStatus(Long authorNo, Long memNo, String status) {
        Optional<ApprovalRoute> optionalApprovalRoute = approvalRouteRepository.findByAuthorization_AuthorNoAndMember_MemNo(authorNo, memNo);
        if (optionalApprovalRoute.isPresent()) {
            ApprovalRoute approvalRoute = optionalApprovalRoute.get();
            approvalRoute.setApprovalStatus(status);
            approvalRouteRepository.save(approvalRoute);
        } else {
            throw new IllegalArgumentException("Approval route not found for the given authorNo and memberNo");
        }
    }

    // ApproNo로 ApprovalRoute를 찾는 메서드
    public Optional<ApprovalRoute> findApprovalRouteByApproNo(Long approNo) {
        return approvalRouteRepository.findById(approNo);
    }

    // 현재 결재 순서에 해당하는 결재자를 조회
    public Optional<ApprovalRoute> getCurrentApprover(Long authorNo, int approvalOrder) {
        return approvalRouteRepository.findByAuthorization_AuthorNoAndApprovalOrder(authorNo, approvalOrder);
    }

    // 결재 경로 생성 로직 (Authorization에 대한 경로를 생성)
    @Transactional
    public void createApprovalRoutesForDocument(Long authorNo, List<Long> approvers, List<Long> referers) {
        Authorization authorization = authorizationRepository.findById(authorNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid authorNo: " + authorNo));

        for (int i = 0; i < approvers.size(); i++) {
            System.out.println("Creating Approval Route for Approver No: " + approvers.get(i));
            createApprovalRoute(authorNo, approvers.get(i), "P", i + 1, true, false, null);
        }

        if (!referers.isEmpty()) {
            System.out.println("Creating Approval Route for Referer No: " + referers.get(0));
            createApprovalRoute(authorNo, referers.get(0), "P", approvers.size() + 1, false, true, null);
        }
    }


    // ApprovalRoute 생성 메서드
    @Transactional
    public ApprovalRoute createApprovalRoute(Long authorNo, Long memNo, String approvalStatus, int approvalOrder, boolean isApprover, boolean isReferer, String signature) {
        // AuthorNo, MemNo 확인
        Authorization authorization = authorizationRepository.findById(authorNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid authorNo"));
        System.out.println("AuthorNo: " + authorization.getAuthorNo());

        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid memberNo"));
        System.out.println("MemberNo: " + member.getMemNo());

        // ApprovalRouteBuilder에 저장할 데이터들 확인
        ApprovalRoute.ApprovalRouteBuilder routeBuilder = ApprovalRoute.builder()
                .authorization(authorization)
                .member(member)
                .approvalStatus(approvalStatus)
                .approvalOrder(approvalOrder)
                .isApprover(isApprover ? "Y" : "N")
                .isReferer(isReferer ? "Y" : "N")
                .rank(member.getRank());
 
        // 결재자인 경우 서명 저장
        if (isApprover) {
            routeBuilder.approverSignature(signature);
            System.out.println("결재자 서명 저장: " + signature);  // 디버깅 로그 추가
        }
 
        // 참조자인 경우 서명 저장
        if (isReferer) {
            routeBuilder.refererSignature(signature);
            System.out.println("참조자 서명 저장: " + signature);  // 디버깅 로그 추가
        }

        // ApprovalRoute 저장 후 로그 출력
        ApprovalRoute approvalRoute = approvalRouteRepository.save(routeBuilder.build());
        System.out.println("ApprovalRoute 저장 완료: " + approvalRoute.getApproNo());

        // 결재자 서명과 참조자 서명 상태 확인
        System.out.println("최종 결재자 서명: " + approvalRoute.getApproverSignature());
        System.out.println("최종 참조자 서명: " + approvalRoute.getRefererSignature());

        return approvalRoute;
    }



    
    @Transactional
    public void updateApprovalRouteToRecalled(Long authorNo) {
        List<ApprovalRoute> approvalRoutes = approvalRouteRepository.findByAuthorization_AuthorNo(authorNo);

        // 모든 관련 결재 경로 상태를 'R'로 변경
        for (ApprovalRoute route : approvalRoutes) {
            if ("P".equals(route.getApprovalStatus())) {  // 대기중인 결재자/참조자만 회수
                route.setApprovalStatus("R");  // 상태를 '회수됨'으로 설정
                approvalRouteRepository.save(route);
            }
        }
    }

    // 결재자, 참조자 알람
    public boolean hasApprovalNotifications(Long memNo) {
        return approvalRouteRepository.existsByMember_MemNoAndApprovalStatus(memNo, "C") 
            || approvalRouteRepository.existsByMember_MemNoAndApprovalStatus(memNo, "R");
    }



}
