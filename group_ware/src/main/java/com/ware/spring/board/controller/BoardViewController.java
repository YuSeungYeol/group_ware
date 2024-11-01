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

    /**
     * 게시판 목록 조회 메서드.
     *
     * ## 기능
     * - 검색 조건에 맞는 게시판 목록을 페이징 처리하여 조회
     * - 검색 DTO와 페이징된 게시판 데이터를 모델에 추가하여 View에서 사용 가능하게 함
     *
     * ## 기술
     * - Pageable 객체를 통해 페이징 및 정렬 정보 설정 (최신순으로 10개씩 조회)
     * - BoardService에서 검색 조건과 페이징 정보에 맞는 게시글 목록을 조회하여 Page 객체로 반환
     * - 검색 조건을 담은 DTO와 결과 리스트를 Model에 추가하여 템플릿에서 접근 가능하게 처리
     *
     * @param model Thymeleaf 모델 객체
     * @param pageable 페이지 설정 객체 (페이징 및 정렬 정보)
     * @param searchDto 게시판 검색 조건을 담고 있는 DTO
     * @return String - 게시판 목록 View 이름
     */
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
    
    /**
     * 게시판 등록 페이지로 이동하는 메서드.
     *
     * ## 기능
     * - 현재 로그인한 사용자의 정보를 조회하여 게시글 작성에 필요한 사용자 정보를 모델에 추가
     * - 사용자의 랭크 정보를 함께 모델에 추가하여 View에서 사용할 수 있도록 처리
     *
     * ## 기술
     * - Principal 객체를 통해 현재 로그인된 사용자 ID를 가져와 Member 정보를 조회
     * - 사용자 정보를 Thymeleaf 모델에 추가하여 게시글 작성 View에서 접근 가능하게 설정
     * - 사용자의 랭크 정보도 모델에 추가하여 등록 페이지에서 사용 가능하게 함
     *
     * @param model Thymeleaf 모델 객체
     * @param session 현재 사용자 세션 객체
     * @param principal 인증된 사용자의 ID 정보를 담고 있는 Principal 객체
     * @return String - 게시글 작성 페이지 View 이름
     */
    @GetMapping("/board/boardCreate")
    public String createBoardPage(Model model, HttpSession session, Principal principal) {
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));

        model.addAttribute("loggedInUser", loggedInMember);
        model.addAttribute("userRankNo", loggedInMember.getRank().getRankNo()); // Rank 정보를 모델에 추가
        return "board/boardCreate";
    }

    /**
     * 게시판 상세 화면으로 이동하는 메서드.
     *
     * ## 기능
     * - 특정 게시글 번호(board_no)에 해당하는 게시글 정보를 조회하여 상세 화면에 표시
     * - 조회한 게시글의 조회수를 1 증가시킴
     * - 현재 로그인한 사용자의 정보를 모델에 추가하여 View에서 접근 가능하게 함
     *
     * ## 기술
     * - Principal 객체를 사용하여 로그인된 사용자의 ID를 조회하고, 이를 통해 Member 정보 조회
     * - BoardDto 객체로 게시글 데이터를 조회하고, 조회수를 증가시키기 위해 서비스 메서드 호출
     * - 게시글 정보와 사용자 정보를 Thymeleaf 모델에 추가하여 View에서 활용 가능하게 설정
     *
     * @param model Thymeleaf 모델 객체
     * @param board_no 조회할 게시글의 번호
     * @param principal 현재 로그인된 사용자의 ID 정보를 담고 있는 Principal 객체
     * @return String - 게시글 상세 화면 View 이름
     */
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

    /**
     * 게시판 수정 화면으로 이동하는 메서드.
     *
     * ## 기능
     * - 특정 게시글 번호(board_no)에 해당하는 게시글 정보를 조회하여 수정 화면에 표시
     * - 게시글 수정 시 필요한 데이터(제목, 내용 등)를 모델에 추가하여 View에서 접근 가능하게 함
     *
     * ## 기술
     * - 서비스 메서드를 호출하여 게시글 번호에 해당하는 BoardDto 객체를 조회
     * - 조회한 게시글 데이터를 모델에 추가하여 Thymeleaf에서 활용 가능하게 설정
     *
     * @param board_no 수정할 게시글의 번호
     * @param model Thymeleaf 모델 객체
     * @return String - 게시글 수정 화면 View 이름
     */
    @GetMapping("/board/update/{board_no}")
	public String updateBoardPage(@PathVariable("board_no")Long board_no,
			Model model) {
		BoardDto dto = boardService.selectBoardOne(board_no);
		model.addAttribute("dto",dto);
		return "board/boardUpdate";
	}
}
