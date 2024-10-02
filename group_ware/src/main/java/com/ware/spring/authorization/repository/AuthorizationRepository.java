package com.ware.spring.authorization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ware.spring.authorization.domain.Authorization;

public interface AuthorizationRepository extends JpaRepository<Authorization, Long> {

	Authorization findByAuthorNo(Long AuthorNo);

	List<Authorization> findByAuthorStatus(String string);

	List<Authorization> findByAuthorStatusNot(String string);
	
	List<Authorization> findByMember_MemNo(Long memNo);

	List<Authorization> findAllByAuthorStatusIn(List<String> statuses);

	Optional<Authorization> findByAuthorNoAndAuthorStatusIn(Long authorNo, List<String> statuses);

	List<Authorization> findByAuthorStatusAndMember_MemNo(String string, Long memNo);

	List<Authorization> findByMember_MemNoAndAuthorStatusIn(Long memNo, List<String> statuses);

	
}
