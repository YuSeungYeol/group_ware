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
    
    /**
     * 게시판 목록을 조회하는 메서드.
     *
     * ## 기능
     * - 검색 조건이 있는 경우 해당 조건을 기반으로 게시판 목록을 조회
     * - 검색 조건이 없으면 전체 게시판 목록을 조회하여 페이지 단위로 반환
     *
     * ## 기술
     * - 제목 검색 조건이 포함된 경우 JPA Repository 메서드 findByBoardTitleContaining을 사용하여 조건에 맞는 데이터를 페이징 처리하여 조회
     * - 검색 조건이 없을 때는 findAll 메서드를 사용하여 전체 데이터를 페이징 처리하여 조회
     *
     * @param searchDto 검색 조건이 포함된 BoardDto 객체
     * @param pageable 페이지네이션 정보가 포함된 Pageable 객체
     * @return Page<Board> - 조회된 게시판 목록의 페이지 객체
     */
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
    
    /**
     * 새로운 게시글을 등록하는 메서드.
     *
     * ## 기능
     * - 전달된 BoardDto 객체와 작성자 정보를 바탕으로 새로운 게시글을 생성하고 저장
     * - 작성자의 mem_no 값을 설정하여 게시글의 작성자 정보와 연결
     *
     * ## 기술
     * - BoardDto 객체를 Entity로 변환하고, 작성자 정보를 추가하여 데이터베이스에 저장
     * - JPA Repository의 save 메서드를 통해 영속성을 관리하여 게시글을 저장
     *
     * @param dto 게시글 정보를 담은 BoardDto 객체
     * @param member 게시글 작성자 정보가 담긴 Member 객체
     * @return Board - 저장된 게시글 Entity
     */
    public Board createBoard(BoardDto dto, Member member) {
        Board board = dto.toEntity();
        board.setMember(member);  // mem_no 값을 설정
        return boardRepository.save(board); 
    }
    
    /**
     * 특정 게시글을 조회하여 상세 정보를 반환하는 메서드.
     *
     * ## 기능
     * - 게시글 번호에 해당하는 게시글을 데이터베이스에서 조회
     * - 조회한 게시글 정보를 BoardDto 객체로 변환하여 반환
     *
     * ## 기술
     * - JPA Repository를 사용하여 게시글 번호로 게시글을 검색
     * - Board 엔티티를 BoardDto로 변환하는 toDto 메서드를 호출하여 DTO를 생성
     *
     * @param board_no 조회할 게시글의 고유 번호
     * @return BoardDto - 게시글 상세 정보를 담고 있는 DTO 객체
     */
    public BoardDto selectBoardOne(Long board_no) {
        Board origin = boardRepository.findByBoardNo(board_no);
        BoardDto dto = new BoardDto().toDto(origin);
        return dto;
    }
    
    /**
     * 게시글의 조회수를 증가시키는 메서드.
     *
     * ## 기능
     * - 게시글 번호를 기반으로 게시글을 조회
     * - 게시글의 조회수를 1 증가시키고 업데이트
     *
     * ## 기술
     * - JPA Repository를 사용하여 게시글 ID로 게시글을 검색
     * - 조회수 필드를 증가시키기 위해 현재 조회수를 가져오고, 1을 더하여 다시 저장
     *
     * @param boardNo 조회수를 증가시킬 게시글의 고유 번호
     * @throws RuntimeException 게시글을 찾을 수 없을 경우 발생
     */
    public void increaseViewCount(Long boardNo) {
        Board board = boardRepository.findById(boardNo).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        board.setBoardView(board.getBoardView() + 1);
        boardRepository.save(board);
    }
    
    /**
     * 게시글을 수정하는 메서드.
     *
     * ## 기능
     * - 게시글 번호를 사용하여 기존 게시글 정보를 조회
     * - 게시글 제목과 내용을 업데이트
     * - 수정된 게시글 정보를 데이터베이스에 저장
     *
     * ## 기술
     * - 게시글 정보를 조회하기 위해 `selectBoardOne` 메서드를 호출
     * - DTO에서 새로운 제목과 내용을 설정한 후, 엔티티로 변환하여 저장
     * - JPA Repository를 사용하여 게시글 정보를 데이터베이스에 업데이트
     *
     * @param dto 수정할 게시글 정보가 담긴 DTO
     * @return Board 수정된 게시글 엔티티
     */
    public Board updateBoard(BoardDto dto) {
        BoardDto temp = selectBoardOne(dto.getBoardNo());
        temp.setBoardTitle(dto.getBoardTitle());
        temp.setBoardContent(dto.getBoardContent());
        
        Board board = temp.toEntity();
        Board result = boardRepository.save(board);
        return result;
    }
    
    /**
     * 게시글을 삭제하는 메서드.
     *
     * ## 기능
     * - 게시글 번호를 기반으로 게시글을 삭제
     * - 삭제 성공 시 1, 실패 시 0을 반환
     *
     * ## 기술
     * - JPA Repository의 `deleteById` 메서드를 사용하여 게시글을 삭제
     * - 삭제 과정에서 발생할 수 있는 예외를 처리하여 안정성을 확보
     *
     * @param board_no 삭제할 게시글의 번호
     * @return int 삭제 결과 (1: 성공, 0: 실패)
     */
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
