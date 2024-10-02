package com.ware.spring.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ware.spring.board.domain.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
	
	Board findByBoardNo(Long BoardNo);
	
	// 삭제되지 않은 게시판 조회
    Page<Board> findByDeleteYn(String deleteYn, Pageable pageable);

    // 제목으로 검색 및 삭제 여부 필터링
    Page<Board> findByBoardTitleContainingAndDeleteYn(String keyword, String deleteYn, Pageable pageable);

	Page<Board> findByBoardTitleContaining(String boardTitle, Pageable pageable);
    
}
