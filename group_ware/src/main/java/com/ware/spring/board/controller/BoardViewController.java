package com.ware.spring.board.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Sort;

import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.board.domain.Board;
import com.ware.spring.board.domain.BoardDto;
import com.ware.spring.board.service.BoardService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BoardViewController {

    private final BoardService boardService;
    private final MemberRepository memberRepository;
    
    @Autowired
    public BoardViewController(BoardService boardService, MemberRepository memberRepository) {
        this.boardService = boardService;
        this.memberRepository = memberRepository;
    }

    // 게시판 목록 조회
    @GetMapping("/board/boardList")
    public String selectBoardList(Model model,
        @PageableDefault(page = 0, size = 10, sort = "boardRegDate", direction = Sort.Direction.DESC) Pageable pageable,
        @ModelAttribute("searchDto") BoardDto searchDto) {

        // 서비스에서 게시판 목록을 가져와서 페이지 데이터로 받습니다.
        Page<Board> resultList = boardService.selectBoardList(searchDto, pageable);
        
        // 모델에 게시판 데이터 및 검색 정보를 추가합니다.
        model.addAttribute("resultList", resultList);
        model.addAttribute("searchDto", searchDto);
        return "board/boardlist";  // 템플릿을 반환합니다.
    }
    
    // 게시판 등록
    @GetMapping("/board/boardCreate")
    public String createBoardPage(Model model, HttpSession session, Principal principal) {
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));

        model.addAttribute("loggedInUser", loggedInMember);
        model.addAttribute("userRankNo", loggedInMember.getRank().getRankNo()); // Rank 정보를 모델에 추가
        return "board/boardCreate";
    }

    // 게시판 상세화면
    @GetMapping("/board/{board_no}")
    public String selectBoardOne(Model model, 
    		@PathVariable("board_no") Long board_no, Principal principal) {
        // 로그인한 사용자 정보 가져오기
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));

        boardService.increaseViewCount(board_no);
        
        // 게시판 데이터
        BoardDto dto = boardService.selectBoardOne(board_no);

        model.addAttribute("dto", dto);
        model.addAttribute("loggedInUser", loggedInMember); // 로그인한 사용자 정보 전달
        return "board/boardDetail";
    }

    // 게시판 수정
    @GetMapping("/board/update/{board_no}")
	public String updateBoardPage(@PathVariable("board_no")Long board_no,
			Model model) {
		BoardDto dto = boardService.selectBoardOne(board_no);
		model.addAttribute("dto",dto);
		return "board/boardUpdate";
	}
}
