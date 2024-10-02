package com.ware.spring.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ware.spring.notice.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

	Page<Notice> findBynoticeTitleContaining(String keyword, Pageable pageable);
	
	Notice findByNoticeNo(Long noticeNo);
	
	// 삭제되지 않은 공지사항 조회
    Page<Notice> findByDeleteYn(String deleteYn, Pageable pageable);

    // 제목으로 검색 및 삭제 여부 필터링
    Page<Notice> findByNoticeTitleContainingAndDeleteYn(String keyword, String deleteYn, Pageable pageable);
}
