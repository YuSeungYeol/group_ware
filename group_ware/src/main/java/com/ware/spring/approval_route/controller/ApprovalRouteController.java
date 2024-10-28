package com.ware.spring.approval_route.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /**
     * 기안 완료된 문서의 결재 경로를 조회하는 API 엔드포인트.
     * 
     * 이 메서드는 특정 문서(authorNo)에 대한 결재 경로를 조회합니다. 
     * 각 결재 경로 DTO에는 결재자 및 참조자 정보가 포함되어 있으며, 
     * 결재자 및 참조자의 서명도 포함됩니다. 
     * 
     * @param authorNo 조회할 문서의 고유 번호. Authorization 엔티티의 식별자입니다.
     * @return ResponseEntity<List<ApprovalRouteDto>> 기안 완료된 문서의 결재 경로 정보를 포함하는 HTTP 응답.
     *         결재 경로 DTO 리스트를 반환하며, 성공적인 조회 시 HTTP 200 OK 상태 코드가 반환됩니다.
     * @throws RuntimeException Authorization 또는 ApprovalRoute를 찾지 못한 경우 예외를 발생시킵니다.
     */
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
    
    /**
     * 기안 완료된 문서의 결재 경로를 조회하는 API 엔드포인트.
     * 
     * @param authorNo 조회할 문서의 고유 번호
     * @return 결재 경로 DTO 리스트를 포함한 ResponseEntity
     */
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

    
    /**
     * 특정 결재자 또는 참조자의 결재 상태를 업데이트하는 API 엔드포인트.
     * 
     * 이 메서드는 문서의 특정 결재자 또는 참조자의 결재 상태를 업데이트합니다.
     * 상태 값은 "Y" (승인), "N" (반려), "P" (대기) 등으로 설정할 수 있습니다.
     * 
     * @param authorNo 업데이트할 문서의 고유 번호. 이 값은 Authorization 엔티티의 식별자입니다.
     * @param memNo 업데이트할 결재자 또는 참조자의 고유 번호. 이 값은 Member 엔티티의 식별자입니다.
     * @param status 업데이트할 결재 상태. 사용 가능한 값은 "Y", "N", "P"입니다.
     * @return HTTP 200 OK 응답. 결재 상태 업데이트가 성공적으로 수행된 경우 이 응답을 반환합니다.
     * @throws IllegalArgumentException 제공된 authorNo 또는 memNo가 유효하지 않을 경우 예외를 발생시킵니다.
     */
    @PostMapping("/update")
    public ResponseEntity<Void> updateApprovalStatus(@RequestParam("authorNo") Long authorNo, 
                                                     @RequestParam("MemNo") Long MemNo, 
                                                     @RequestParam("status") String status) {
        approvalRouteService.updateApprovalStatus(authorNo, MemNo, status);  // 상태 업데이트 (예: "Y", "N", "P")
        return ResponseEntity.ok().build();
    }
    
    /**
     * 대리점 번호에 해당하는 직원 목록을 조회하는 API 엔드포인트.
     * 
     * 이 메서드는 주어진 대리점 ID(distributorId)를 기준으로 해당 대리점에 소속된 
     * 직원(Member) 목록을 조회하여 반환합니다. 
     * 
     * @param distributorId 조회할 대리점의 고유 번호. 이 번호는 Distributor 엔티티의 식별자입니다.
     * @return ResponseEntity<List<Member>> 대리점에 소속된 직원 목록을 포함하는 HTTP 응답.
     *         성공적인 조회 시 HTTP 200 OK 상태 코드와 함께 직원 목록이 반환됩니다.
     */
    @GetMapping("/getEmployeesByDistributor")
    public ResponseEntity<List<Member>> getEmployeesByDistributor(@RequestParam("distributorId") Long distributorId) {
        List<Member> employees = memberRepository.findByDistributor_DistributorNo(distributorId);
        return ResponseEntity.ok(employees);
    } 
    
    /**
     * 사용자에 대한 알림 상태를 조회하는 API 엔드포인트.
     * 
     * 이 메서드는 현재 인증된 사용자의 ID(memId)를 기준으로,
     * 해당 사용자가 받은 결재 알림과 기안 알림의 상태를 조회하여
     * 두 개의 불리언 값을 포함하는 맵을 반환합니다.
     * 
     * @return ResponseEntity<Map<String, Boolean>> 알림 상태 정보를 포함하는 HTTP 응답.
     *         성공적으로 조회 시 HTTP 200 OK 상태 코드와 함께
     *         결재 알림과 기안 알림의 상태가 포함된 맵이 반환됩니다.
     */ 
    @GetMapping("/nav")
    public ResponseEntity<Map<String, Boolean>> getNavNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memId = authentication.getName();
        Optional<Member> memberOpt = memberRepository.findByMemId(memId);

        Map<String, Boolean> notifications = new HashMap<>();

        if (memberOpt.isPresent()) {
            Long memNo = memberOpt.get().getMemNo();
            boolean approvalNotification = approvalRouteService.hasApprovalNotifications(memNo);
            boolean authorNotification = authorizationService.hasAuthorNotifications(memNo); 

            notifications.put("approvalNotification", approvalNotification);
            notifications.put("authorNotification", authorNotification);
        }

        return ResponseEntity.ok(notifications);
    }

}