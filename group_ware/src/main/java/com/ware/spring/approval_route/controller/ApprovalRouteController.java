package com.ware.spring.approval_route.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/approval")
public class ApprovalRouteController {

    private final ApprovalRouteService approvalRouteService;
    private final MemberRepository memberRepository;
    private final AuthorizationService authorizationService;
    private final AuthorizationRepository authorizationRepository;
    private final ApprovalRouteRepository approvalRouteRepository;
    
    @Autowired
    public ApprovalRouteController(ApprovalRouteService approvalRouteService, 
    		MemberRepository memberRepository, AuthorizationService authorizationService
    		,AuthorizationRepository authorizationRepository, ApprovalRouteRepository approvalRouteRepository) {
        this.approvalRouteService = approvalRouteService;
        this.memberRepository = memberRepository;
        this.authorizationService = authorizationService;
        this.authorizationRepository = authorizationRepository;
        this.approvalRouteRepository = approvalRouteRepository;
    }

    // 기안 진행중인 문서 조회
    @GetMapping("/{authorNo}")
    public ResponseEntity<List<ApprovalRouteDto>> getApprovalRoutes(@PathVariable("authorNo") Long authorNo) {
        List<ApprovalRouteDto> approvalRouteDtos = approvalRouteService.getApprovalRoutesByAuthorNo(authorNo);

        approvalRouteDtos.forEach(dto -> {
            // 결재자 정보 설정
            if ("Y".equals(dto.getIsApprover())) {
                Member approver = memberRepository.findById(dto.getMemNo()).orElse(null);
                if (approver != null) {
                    dto.setApproverName(approver.getMemName());
                    if (approver.getRank() != null) {
                        dto.setApproverRankName(approver.getRank().getRankName());
                    }
                }
            }

            // 참조자 정보 설정
            if ("Y".equals(dto.getIsReferer())) {
                Member referer = memberRepository.findById(dto.getMemNo()).orElse(null);
                if (referer != null) {
                    dto.setRefererName(referer.getMemName());
                    if (referer.getRank() != null) {
                        dto.setRefererRankName(referer.getRank().getRankName());
                    }
                }
            }
        });

        return ResponseEntity.ok(approvalRouteDtos);
    }
    // 기안 완료된 문서 조회
    @GetMapping("/completed/{authorNo}")
    public ResponseEntity<List<ApprovalRouteDto>> getCompletedApprovalRoutes(@PathVariable("authorNo") Long authorNo) {
        List<ApprovalRouteDto> approvalRouteDtos = approvalRouteService.getApprovalRoutesByAuthorNo(authorNo);

        approvalRouteDtos.forEach(dto -> {
            Authorization authorization = authorizationRepository.findById(dto.getAuthorNo())
                    .orElseThrow(() -> new RuntimeException("Authorization not found"));

            // 결재자 정보 설정
            if ("Y".equals(dto.getIsApprover())) {
                Member approver = memberRepository.findById(dto.getMemNo()).orElse(null);
                if (approver != null) {
                    dto.setApproverName(approver.getMemName());

                    // 결재자의 서명 정보 설정 (ApprovalRoute의 서명 사용)
                    ApprovalRoute route = approvalRouteRepository.findByAuthorization_AuthorNoAndMember_MemNoAndIsApprover(authorization.getAuthorNo(), approver.getMemNo(), "Y")
                            .orElseThrow(() -> new RuntimeException("Approval route not found"));
                    dto.setApproverSignature(route.getApproverSignature());
                }
            }

            // 참조자 정보 설정
            if ("Y".equals(dto.getIsReferer())) {
                Member referer = memberRepository.findById(dto.getMemNo()).orElse(null);
                if (referer != null) {
                    dto.setRefererName(referer.getMemName());

                    // 참조자의 서명 정보 설정 (ApprovalRoute의 서명 사용)
                    ApprovalRoute route = approvalRouteRepository.findByAuthorization_AuthorNoAndMember_MemNoAndIsReferer(authorization.getAuthorNo(), referer.getMemNo(), "Y")
                            .orElseThrow(() -> new RuntimeException("Approval route not found"));
                    dto.setRefererSignature(route.getRefererSignature());
                }
            }
        });

        return ResponseEntity.ok(approvalRouteDtos);
    }

    
    // 특정 결재자 또는 참조자의 결재 상태 업데이트
    @PostMapping("/update")
    public ResponseEntity<Void> updateApprovalStatus(@RequestParam("authorNo") Long authorNo, 
                                                     @RequestParam("MemNo") Long MemNo, 
                                                     @RequestParam("status") String status) {
        approvalRouteService.updateApprovalStatus(authorNo, MemNo, status);  // 상태 업데이트 (예: "Y", "N", "P")
        return ResponseEntity.ok().build();
    }
    
    // 대리점 번호로 직원 조회
    @GetMapping("/getEmployeesByDistributor")
    public ResponseEntity<List<Member>> getEmployeesByDistributor(@RequestParam("distributorId") Long distributorId) {
        List<Member> employees = memberRepository.findByDistributor_DistributorNo(distributorId);
        return ResponseEntity.ok(employees);
    } 

}