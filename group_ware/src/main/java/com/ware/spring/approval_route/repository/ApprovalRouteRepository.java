package com.ware.spring.approval_route.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ware.spring.approval_route.domain.ApprovalRoute;

public interface ApprovalRouteRepository extends JpaRepository<ApprovalRoute, Long> {  // Long 타입으로 변경
    List<ApprovalRoute> findByAuthorization_AuthorNo(Long authorNo);
    List<ApprovalRoute> findByMember_MemNo(Long memNo);

    Optional<ApprovalRoute> findByAuthorization_AuthorNoAndMember_MemNo(Long authorNo, Long memNo);
    Optional<ApprovalRoute> findByAuthorization_AuthorNoAndApprovalOrder(Long authorNo, int approvalOrder);

    // 결재자 정보 조회
    Optional<ApprovalRoute> findByAuthorization_AuthorNoAndMember_MemNoAndIsApprover(Long authorNo, Long memNo, String isApprover);

    // 참조자 정보 조회
    Optional<ApprovalRoute> findByAuthorization_AuthorNoAndMember_MemNoAndIsReferer(Long authorNo, Long memNo, String isReferer);
	
    // 결재 승인, 반려 창 페이징
    Page<ApprovalRoute> findByMember_MemNo(Long memNo, Pageable pageable);
    
    // 알람관련
	boolean existsByMember_MemNoAndApprovalStatus(Long memNo, String string);

    
}
