package com.ware.spring.notice.controller;

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
import com.ware.spring.notice.domain.Notice;
import com.ware.spring.notice.domain.NoticeDto;
import com.ware.spring.notice.service.NoticeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class NoticeViewController {

    private final NoticeService noticeService;
    private final MemberRepository memberRepository;
    
    @Autowired
    public NoticeViewController(NoticeService noticeService, MemberRepository memberRepository) {
        this.noticeService = noticeService;
        this.memberRepository = memberRepository;
    }

    // 공지사항 목록 조회
    @GetMapping("/notice/noticeList")
    public String selectNoticeList(Model model,
        @PageableDefault(page = 0, size = 10, sort = "noticeRegDate", direction = Sort.Direction.DESC) Pageable pageable,
        @ModelAttribute("searchDto") NoticeDto searchDto) {

        // 서비스에서 공지사항 목록을 가져와서 페이지 데이터로 받습니다.
        Page<Notice> resultList = noticeService.selectNoticeList(searchDto, pageable);
        
        // 모델에 공지사항 데이터 및 검색 정보를 추가합니다.
        model.addAttribute("resultList", resultList);
        model.addAttribute("searchDto", searchDto);
        return "notice/noticelist";  // 템플릿을 반환합니다.
    }
    
    // 공지사항 등록
    @GetMapping("/notice/noticeCreate")
    public String createNoticePage(Model model, HttpSession session, Principal principal) {
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));

        model.addAttribute("loggedInUser", loggedInMember);
        model.addAttribute("userRankNo", loggedInMember.getRank().getRankNo()); // Rank 정보를 모델에 추가
        return "notice/noticeCreate";
    } 

    // 공지사항 상세화면

    
    @GetMapping("/notice/{notice_no}")
    public String selectNoticeOne(Model model, 
    		@PathVariable("notice_no") Long notice_no, Principal principal) {
        // 로그인한 사용자 정보 가져오기
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));

        noticeService.increaseViewCount(notice_no);
        
        // 공지사항 데이터
        NoticeDto dto = noticeService.selectNoticeOne(notice_no);

        model.addAttribute("dto", dto);
        model.addAttribute("loggedInUser", loggedInMember); // 로그인한 사용자 정보 전달
        return "notice/noticeDetail";
    }

    
    // 공지사항 수정
    @GetMapping("/notice/update/{notice_no}")
	public String updateNoticePage(@PathVariable("notice_no")Long notice_no,
			Model model) {
		NoticeDto dto = noticeService.selectNoticeOne(notice_no);
		model.addAttribute("dto",dto);
		return "notice/noticeUpdate";
	}
}
