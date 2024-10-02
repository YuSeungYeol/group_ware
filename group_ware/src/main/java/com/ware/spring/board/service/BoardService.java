package com.ware.spring.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ware.spring.member.domain.Member;
import com.ware.spring.board.domain.Board;
import com.ware.spring.board.domain.BoardDto;
import com.ware.spring.board.repository.BoardRepository;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    
    @Autowired
    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }
    
    // Board 목록을 조회하는 메서드가 데이터를 반환해야 합니다.
    public Page<Board> selectBoardList(BoardDto searchDto, Pageable pageable) {
        String boardTitle = searchDto.getBoardTitle();
        if (boardTitle != null && !boardTitle.isEmpty()) {
            // 제목 검색
            return boardRepository.findByBoardTitleContaining(boardTitle, pageable);
        } else {
            // 전체 목록 조회
            return boardRepository.findAll(pageable);
        }
    }
    
    // 게시글 등록
    public Board createBoard(BoardDto dto, Member member) {
        Board board = dto.toEntity();
        board.setMember(member);  // mem_no 값을 설정
        return boardRepository.save(board); 
    }
    
    // 게시글 상세화면 
    public BoardDto selectBoardOne(Long board_no) {
        Board origin = boardRepository.findByBoardNo(board_no);
        BoardDto dto = new BoardDto().toDto(origin);
        return dto;
    }
    
    // 조회수 증가
    public void increaseViewCount(Long boardNo) {
        Board board = boardRepository.findById(boardNo).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        board.setBoardView(board.getBoardView() + 1);
        boardRepository.save(board);
    }
    
    // 게시글 수정
    public Board updateBoard(BoardDto dto) {
        BoardDto temp = selectBoardOne(dto.getBoardNo());
        temp.setBoardTitle(dto.getBoardTitle());
        temp.setBoardContent(dto.getBoardContent());
        
        Board board = temp.toEntity();
        Board result = boardRepository.save(board);
        return result;
    }
    
    // 게시글 삭제
    public int deleteBoard(Long board_no) {
        int result = 0;
        try {
            boardRepository.deleteById(board_no);
            result = 1;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    // 게시글 리스트 조회(삭제 여부 Y,N)
//    public Page<Board> selectBoardList(BoardDto searchDto, Pageable pageable) {
//        String boardTitle = searchDto.getBoardTitle();
//        if (boardTitle != null && !boardTitle.isEmpty()) {
//            // 제목 검색 및 삭제되지 않은 데이터만 조회
//            return boardRepository.findByBoardTitleContainingAndDeleteYn(boardTitle, "n", pageable);
//        } else {
//            // 삭제되지 않은 전체 목록 조회
//            return boardRepository.findByDeleteYn("n", pageable);
//        }
//    }
    
//    ALTER TABLE board ADD COLUMN delete_yn CHAR(1) DEFAULT 'n';
}
