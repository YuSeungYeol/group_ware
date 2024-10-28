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

    /**
     * 특정 authorNo에 대한 결재자와 참조자 상태 조회 메서드
     * 기술: Spring Data JPA, 스트림 API, DTO 변환
     * 설명: 특정 문서 번호(authorNo)를 기준으로 모든 결재자와 참조자의 상태 정보를 조회하고, 이를 `ApprovalRouteDto`로 변환하여 반환합니다.
     *      - 결재자와 참조자의 서명 정보도 포함하여 `ApprovalRouteDto` 객체에 매핑합니다.
     *
     * @param authorNo 조회할 문서 번호
     * @return 결재 경로 목록을 `ApprovalRouteDto` 형태로 반환
     */
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




    /**
     * 특정 authorNo와 memNo에 대한 ApprovalRoute 상태 업데이트
     * 기술: Spring Data JPA, 트랜잭션 관리, 예외 처리
     * 설명: 주어진 문서 번호(authorNo)와 멤버 번호(memNo)에 해당하는 `ApprovalRoute` 엔티티의 결재 상태(ApprovalStatus)를 업데이트합니다.
     *      - 상태가 존재하지 않을 경우 `IllegalArgumentException` 예외를 발생시킵니다.
     * 
     * @param authorNo 업데이트할 문서 번호
     * @param memNo 업데이트할 멤버 번호
     * @param status 새로 설정할 결재 상태
     * @throws IllegalArgumentException 주어진 authorNo와 memNo에 해당하는 결재 경로가 없을 경우
     */
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

    /**
     * ApproNo로 ApprovalRoute 찾기
     * 기술: Spring Data JPA
     * 설명: 주어진 결재 경로 번호(approNo)에 해당하는 `ApprovalRoute` 엔티티를 검색합니다.
     *      - 결재 경로가 존재하지 않으면 `Optional.empty()`를 반환합니다.
     *
     * @param approNo 검색할 결재 경로 번호
     * @return 주어진 approNo에 해당하는 ApprovalRoute의 Optional 객체
     */
    public Optional<ApprovalRoute> findApprovalRouteByApproNo(Long approNo) {
        return approvalRouteRepository.findById(approNo);
    }

    /**
     * 현재 결재 순서에 해당하는 결재자 조회
     * 기술: Spring Data JPA
     * 설명: 주어진 문서 번호(authorNo)와 결재 순서(approvalOrder)에 따라 현재 결재 순서에 해당하는 
     *      `ApprovalRoute` 엔티티를 검색합니다.
     *      - 해당 조건을 만족하는 결재 경로가 없을 경우 `Optional.empty()`를 반환합니다.
     *
     * @param authorNo 문서 번호
     * @param approvalOrder 조회할 결재 순서
     * @return 주어진 authorNo와 approvalOrder에 해당하는 ApprovalRoute의 Optional 객체
     */
    public Optional<ApprovalRoute> getCurrentApprover(Long authorNo, int approvalOrder) {
        return approvalRouteRepository.findByAuthorization_AuthorNoAndApprovalOrder(authorNo, approvalOrder);
    }

    /**
     * 결재 경로 생성 로직 (Authorization에 대한 경로를 생성)
     * 기술: Spring Data JPA, 트랜잭션 관리
     * 설명: 주어진 문서(authorNo)에 대해 결재자와 참조자를 위한 `ApprovalRoute` 엔티티들을 생성합니다.
     *      - approvers 목록에 있는 각 결재자를 위한 경로를 생성하여 승인 대기("P") 상태와 순서를 지정합니다.
     *      - 참조자가 있는 경우, 참조자 목록의 첫 번째 사용자에 대해 결재 경로를 생성합니다.
     * 
     * @param authorNo 결재 경로를 생성할 문서 번호
     * @param approvers 결재자로 지정할 사용자 번호 목록
     * @param referers 참조자로 지정할 사용자 번호 목록
     */
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


    /**
     * ApprovalRoute 생성 메서드
     * 기술: Spring Data JPA, 트랜잭션 관리
     * 설명: 주어진 문서(`authorNo`)와 사용자(`memNo`)에 대한 `ApprovalRoute` 객체를 생성하여 결재 경로 정보를 저장합니다.
     *      - `Authorization`과 `Member` 엔티티의 유효성을 확인 후 결재 경로 생성
     *      - 결재자일 경우 서명(`approverSignature`) 정보를 저장하고, 참조자인 경우 `refererSignature` 정보를 저장
     * 
     * @param authorNo 결재 경로가 속할 문서 번호
     * @param memNo 결재 경로에 참여하는 사용자 번호
     * @param approvalStatus 초기 결재 상태 (대기 "P", 승인 "Y", 반려 "N" 등)
     * @param approvalOrder 결재 순서
     * @param isApprover 결재자 여부 (true이면 결재자)
     * @param isReferer 참조자 여부 (true이면 참조자)
     * @param signature 결재자/참조자의 서명 정보
     * @return 생성된 `ApprovalRoute` 엔티티
     * @throws IllegalArgumentException 유효하지 않은 `authorNo` 또는 `memNo`인 경우
     */
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

    /**
     * 결재 경로를 회수된 상태로 업데이트하는 메서드
     * 기술: Spring Data JPA, 트랜잭션 관리
     * 설명: 주어진 문서(`authorNo`)와 관련된 모든 결재 경로의 상태를 '회수됨(R)'으로 변경합니다.
     *      - 상태가 '대기중(P)'인 결재자 및 참조자만 대상으로 하여 상태 변경
     * 
     * @param authorNo 문서 번호 (Authorization)
     */
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

    /**
     * 결재자 또는 참조자의 알림 여부를 확인하는 메서드
     * 기술: Spring Data JPA
     * 설명: 주어진 회원(`memNo`)이 결재자로서 또는 참조자로서 '승인완료(C)' 또는 '회수됨(R)' 상태의 
     * 알림이 존재하는지를 확인합니다.
     * 
     * @param memNo 회원 번호
     * @return 알림이 존재하면 true, 그렇지 않으면 false
     */
    public boolean hasApprovalNotifications(Long memNo) {
        return approvalRouteRepository.existsByMember_MemNoAndApprovalStatus(memNo, "C") 
            || approvalRouteRepository.existsByMember_MemNoAndApprovalStatus(memNo, "R");
    }



}
