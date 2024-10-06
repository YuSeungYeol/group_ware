package com.ware.spring.notice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ware.spring.notice.domain.NoticeStatus;

public interface NoticeStatusRepository extends JpaRepository<NoticeStatus, Long>{

	List<NoticeStatus> findByMember_MemNoAndIsRead(Long memNo, String isRead);

	boolean existsByMember_MemNoAndIsRead(Long memNo, String string);

	@Query("SELECT ns FROM NoticeStatus ns WHERE ns.notice.noticeNo = :noticeNo AND ns.member.memNo = :memNo")
	Optional<NoticeStatus>findByNotice_NoticeNoAndMember_MemNo(@Param("noticeNo") Long noticeNo, @Param("memNo") Long memNo);

}
