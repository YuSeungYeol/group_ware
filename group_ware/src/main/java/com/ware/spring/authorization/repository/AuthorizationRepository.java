package com.ware.spring.authorization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.domain.AuthorizationDto;

public interface AuthorizationRepository extends JpaRepository<Authorization, Long> {

	Authorization findByAuthorNo(Long AuthorNo);

	List<Authorization> findByAuthorStatus(String string);

	List<Authorization> findByAuthorStatusNot(String string);
	
	List<Authorization> findByMember_MemNo(Long memNo);

	List<Authorization> findAllByAuthorStatusIn(List<String> statuses);

	Optional<Authorization> findByAuthorNoAndAuthorStatusIn(Long authorNo, List<String> statuses);

	Page<Authorization> findByAuthorStatusAndMember_MemNo(String authorStatus, Long memNo, Pageable pageable);

	List<Authorization> findByMember_MemNoAndAuthorStatusIn(Long memNo, List<String> statuses);

	// 기안 진행 중인 문서 조회
    Page<Authorization> findByAuthorStatus(String status, Pageable pageable);

    // 완료된 문서(승인/반려) 조회
    Page<Authorization> findByAuthorStatusIn(List<String> statuses, Pageable pageable);

    // 알람 관련
	boolean existsByMember_MemNoAndAuthorStatusNot(Long memNo, String string);

	// 알람 삭제
	Optional<Authorization> findByAuthorNoAndMember_MemNo(Long authorNo, Long memNo);



}
