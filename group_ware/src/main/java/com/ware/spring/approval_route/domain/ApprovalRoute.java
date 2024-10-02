package com.ware.spring.approval_route.domain;

import java.time.LocalDateTime;

import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.domain.Rank;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_route")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
public class ApprovalRoute {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="appro_no")
	private Long approNo;

	@ManyToOne(fetch = FetchType.LAZY)  // 양방향 관계 설정 (다대일)
    @JoinColumn(name = "author_no")  // 외래 키로 사용되는 컬럼
    private Authorization authorization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mem_no")
    private Member member;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "approval_order")
    private int approvalOrder;

    @Column(name = "is_approver", length = 1)
    private String isApprover;

    @Column(name = "is_referer", length = 1)
    private String isReferer;
    
    @ManyToOne
    @JoinColumn(name = "rank_no")
    private Rank rank; // Rank 필드 추가

    @Column(name = "approver_signature")
    private String approverSignature;

    @Column(name = "referer_signature")
    private String refererSignature;
}
