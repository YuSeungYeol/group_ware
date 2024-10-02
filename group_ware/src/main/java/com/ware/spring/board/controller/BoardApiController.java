package com.ware.spring.board.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.board.service.BoardService;

import jakarta.servlet.http.HttpSession;

import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.board.domain.BoardDto;


@Controller
public class BoardApiController {

    private final BoardService boardService;
    private final MemberRepository memberRepository;

    @Autowired
    public BoardApiController(BoardService boardService, MemberRepository memberRepository) {
        this.boardService = boardService;
        this.memberRepository = memberRepository;
    }

    // 게시글 등록
    @ResponseBody
    @PostMapping("/board")
    public Map<String, String> createBoard(BoardDto dto, Principal principal, HttpSession session) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "게시글 등록 중 오류가 발생했습니다.");

        // 로그인한 사용자 정보 가져오기 (Principal 또는 Session)
        String username;
        if (principal != null) {
            username = principal.getName();
        } else {
            Member loggedInMember = (Member) session.getAttribute("loggedInUser");
            if (loggedInMember == null) {
                resultMap.put("res_msg", "로그인이 필요합니다.");
                return resultMap;
            }
            username = loggedInMember.getMemId();
        }

        // Member 조회
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));

        // dto에 Member 객체 설정
        dto.setMember(loggedInMember);
        dto.setBoardView(0);  // 조회수 기본값 설정

        if (boardService.createBoard(dto, loggedInMember) != null) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "게시글이 성공적으로 등록되었습니다.");
        }

        return resultMap;
    }

    // 게시글 수정
    @ResponseBody
    @PostMapping("/board/{board_no}")
    public Map<String, String> updateBoard(BoardDto dto, Principal principal) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "게시글 수정 중 오류가 발생했습니다.");

        // 로그인한 사용자의 memNo 가져오기
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));
        
        // 수정할 게시글의 작성자 확인
        BoardDto originalDto = boardService.selectBoardOne(dto.getBoardNo());
        if (!originalDto.getMember().getMemNo().equals(loggedInMember.getMemNo())) {
            resultMap.put("res_msg", "본인이 작성한 글만 수정할 수 있습니다.");
            return resultMap;
        }

        // 제목과 내용 업데이트
        if (boardService.updateBoard(dto) != null) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "게시글이 성공적으로 수정되었습니다.");
        }

        return resultMap;
    }

    // 게시글 삭제
    @ResponseBody
    @DeleteMapping("/board/{board_no}")
    public Map<String, String> deleteBoard(@PathVariable("board_no") Long board_no, Principal principal) {
        Map<String, String> map = new HashMap<>();
        map.put("res_code", "404");
        map.put("res_msg", "게시글 삭제 중 오류가 발생했습니다.");

        // 로그인한 사용자 정보 가져오기
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));

        // 삭제할 게시글의 작성자 확인
        BoardDto originalDto = boardService.selectBoardOne(board_no);
        if (!originalDto.getMember().getMemNo().equals(loggedInMember.getMemNo())) {
            map.put("res_msg", "본인이 작성한 글만 삭제할 수 있습니다.");
            return map;
        }

        // 실제 삭제
        if (boardService.deleteBoard(board_no) > 0) {
            map.put("res_code", "200");
            map.put("res_msg", "정상적으로 게시글이 삭제되었습니다.");
        }

        return map;
    }


    // 게시글 삭제(삭제 여부 Y,N)
//    @ResponseBody
//    @DeleteMapping("/board/{board_no}")
//    public Map<String, String> deleteBoard(@PathVariable("board_no") Long board_no) {
//        Map<String, String> map = new HashMap<>();
//        map.put("res_code", "404");
//        map.put("res_msg", "게시글 삭제 중 오류가 발생했습니다.");
//
//        // 게시글 삭제 여부 업데이트 (실제 삭제가 아닌 delete_yn을 'y'로 업데이트)
//        Board board = boardRepository.findById(board_no).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
//        board.setDeleteYn("y");
//        boardRepository.save(board);
//
//        map.put("res_code", "200");
//        map.put("res_msg", "정상적으로 게시글이 삭제되었습니다.");
//        return map;
//    }

}
