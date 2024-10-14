package com.ware.spring.approval_route.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.member.domain.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ApprovalRouteDto {

    private Long approNo;
    private Long authorNo;
    private Long memNo;
    private String memberName;
    private String approvalStatus;
    private LocalDateTime approvedDate;
    private int approvalOrder;
    private String isApprover;  // "Y" or "N"
    private String isReferer;   // "Y" or "N"

    // 결재자 및 참조자의 이름과 직급
    private String approverName;
    private String approverRankName;  // 직급 이름을 문자열로 저장
    private String refererName;
    private String refererRankName;   // 직급 이름을 문자열로 저장

    private String approverSignature;  // 결재자의 사인
    private String refererSignature;   // 참조자의 사인

    // ApprovalRoute 객체를 DTO로 변환하는 메서드
    public static ApprovalRouteDto toDto(ApprovalRoute approvalRoute, Member approver, Member referer) {
        String approverSignature = approvalRoute.getAuthorization() != null ? approvalRoute.getAuthorization().getSignature() : null;
        String refererSignature = approvalRoute.getAuthorization() != null ? approvalRoute.getAuthorization().getSignature() : null;

        return ApprovalRouteDto.builder()
            .approNo(approvalRoute.getApproNo())
            .authorNo(approvalRoute.getAuthorization() != null ? approvalRoute.getAuthorization().getAuthorNo() : null)
            .memNo(approvalRoute.getMember() != null ? approvalRoute.getMember().getMemNo() : null)
            .memberName(approvalRoute.getMember() != null ? approvalRoute.getMember().getMemName() : null)
            .approvalStatus(approvalRoute.getApprovalStatus())
            .approvedDate(approvalRoute.getApprovedDate())
            .approvalOrder(approvalRoute.getApprovalOrder())
            .isApprover(approvalRoute.getIsApprover())
            .isReferer(approvalRoute.getIsReferer())
            .approverName(approver != null ? approver.getMemName() : null)
            .approverRankName(approver != null && approver.getRank() != null ? approver.getRank().getRankName() : null)
            .refererName(referer != null ? referer.getMemName() : null)
            .refererRankName(referer != null && referer.getRank() != null ? referer.getRank().getRankName() : null)
            .approverSignature(approverSignature)  // Authorization에서 사인 정보 가져오기
            .refererSignature(refererSignature)    // Authorization에서 사인 정보 가져오기
            .build();
    }

    // DTO를 Entity로 변환하는 메서드
    public static ApprovalRouteDto toDto(ApprovalRoute approvalRoute) {
        Authorization authorization = approvalRoute.getAuthorization();  // Authorization 엔티티 가져오기
        
        return ApprovalRouteDto.builder()
            .approNo(approvalRoute.getApproNo())
            .authorNo(authorization != null ? authorization.getAuthorNo() : null)
            .memNo(approvalRoute.getMember() != null ? approvalRoute.getMember().getMemNo() : null)
            .memberName(approvalRoute.getMember() != null ? approvalRoute.getMember().getMemName() : null)
            .approvalStatus(approvalRoute.getApprovalStatus())
            .approvedDate(approvalRoute.getApprovedDate())
            .approvalOrder(approvalRoute.getApprovalOrder())
            .isApprover(approvalRoute.getIsApprover())
            .isReferer(approvalRoute.getIsReferer())
            .approverSignature(null)  // 기본값 null 설정
            .refererSignature(null)   // 기본값 null 설정
            .build();
    }
 
    @JsonCreator
    public ApprovalRouteDto(
            @JsonProperty("memNo") Long memNo, 
            @JsonProperty("isApprover") String isApprover, 
            @JsonProperty("isReferer") String isReferer) {
        this.memNo = memNo;
        this.isApprover = isApprover;
        this.isReferer = isReferer;
    }
}
