package com.ware.spring.notice.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
import com.ware.spring.notice.domain.NoticeStatusDto;
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

    /**
     * 공지사항 목록을 조회하여 뷰에 전달하는 메서드.
     *
     * ## 기능
     * - 공지사항 목록을 검색 조건과 페이지 정보를 기준으로 조회
     * - 조회된 결과를 모델에 추가하여 뷰로 전달
     *
     * ## 기술
     * - `PageableDefault`를 통해 페이지 번호, 크기, 정렬 순서를 설정하여 페이지 단위로 데이터 조회
     * - 검색 조건을 위한 `NoticeDto`와 `Pageable`을 서비스에 전달하여 조건에 맞는 공지사항 목록을 페이징 처리
     * - 모델에 조회 결과 및 검색 조건을 추가하여 뷰에 전달
     *
     * @param model 공지사항 목록과 검색 정보를 담는 모델 객체
     * @param pageable 페이지 설정 객체로, 기본값은 첫 페이지, 페이지당 10개 항목, 등록일 기준 내림차순
     * @param searchDto 검색 조건을 담은 DTO 객체
     * @return String - 공지사항 목록 페이지 뷰 이름
     */
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
    
    /**
     * 공지사항 등록 페이지를 반환하는 메서드.
     *
     * ## 기능
     * - 로그인한 사용자의 정보를 가져와 모델에 추가
     * - 사용자의 계급 정보를 통해 공지사항 등록 권한을 검토할 수 있도록 준비
     * - 공지사항 작성 페이지 뷰를 반환
     *
     * ## 기술
     * - `Principal`을 통해 로그인한 사용자의 ID를 확인
     * - `memberRepository`를 사용하여 사용자 ID로 회원 정보를 조회
     * - 모델에 로그인 사용자 정보와 계급 번호를 추가하여 뷰에서 참조 가능하게 설정
     *
     * @param model 공지사항 등록 페이지에 필요한 데이터를 담는 모델 객체
     * @param session 현재 사용자 세션
     * @param principal 현재 로그인한 사용자 인증 객체
     * @return String - 공지사항 등록 페이지 뷰 이름
     */
    @GetMapping("/notice/noticeCreate")
    public String createNoticePage(Model model, HttpSession session, Principal principal) {
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));

        model.addAttribute("loggedInUser", loggedInMember);
        model.addAttribute("userRankNo", loggedInMember.getRank().getRankNo()); // Rank 정보를 모델에 추가
        return "notice/noticeCreate";
    }   
 
    /**
     * 공지사항 상세 정보를 조회하고 상세 페이지를 반환하는 메서드.
     *
     * ## 기능
     * - 로그인한 사용자의 정보를 가져와 모델에 추가
     * - 해당 공지사항의 조회수를 증가시키고, 공지사항 세부 정보를 가져옴
     * - 조회한 공지사항 데이터를 모델에 추가하여 뷰에서 출력할 수 있도록 설정
     *
     * ## 기술
     * - `Principal`을 통해 현재 로그인한 사용자 ID를 확인
     * - `memberRepository`로 사용자 ID를 사용해 `Member` 정보를 조회하여 로그인 사용자 정보를 확보
     * - `noticeService`의 `increaseViewCount` 메서드를 호출하여 조회수 증가 처리
     * - `noticeService`의 `selectNoticeOne`을 통해 공지사항 데이터를 조회
     * - 모델에 공지사항 정보 및 로그인 사용자 정보를 추가하여 뷰에 전달
     *
     * @param model 공지사항 상세 페이지에 필요한 데이터를 담는 모델 객체
     * @param notice_no 조회할 공지사항의 고유 번호
     * @param principal 현재 로그인한 사용자 인증 객체
     * @return String - 공지사항 상세 페이지 뷰 이름
     */
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

    
    /**
     * 공지사항 수정 페이지로 이동하고, 수정할 공지사항 정보를 모델에 추가하는 메서드.
     *
     * ## 기능
     * - 공지사항 번호를 통해 해당 공지사항 데이터를 조회하여 모델에 추가
     * - 수정할 공지사항 정보를 뷰에 전달하여 초기값을 설정
     *
     * ## 기술
     * - `noticeService`의 `selectNoticeOne` 메서드를 호출하여 수정할 공지사항의 상세 데이터를 가져옴
     * - 모델 객체에 공지사항 데이터를 추가하여 뷰에서 출력할 수 있도록 설정
     *
     * @param notice_no 수정할 공지사항의 고유 번호
     * @param model 공지사항 수정 페이지에 필요한 데이터를 담는 모델 객체
     * @return String - 공지사항 수정 페이지 뷰 이름
     */
    @GetMapping("/notice/update/{notice_no}")
	public String updateNoticePage(@PathVariable("notice_no")Long notice_no,
			Model model) {
		NoticeDto dto = noticeService.selectNoticeOne(notice_no);
		model.addAttribute("dto",dto);
		return "notice/noticeUpdate";
	}
    
    /**
     * 특정 회원의 읽지 않은 공지사항 목록을 조회하는 API 메서드.
     *
     * ## 기능
     * - 현재 로그인한 사용자의 ID(`memId`)를 통해 회원 정보를 확인하고, 해당 회원이 읽지 않은 공지사항 목록을 조회
     * - 조회한 공지사항 목록을 DTO 형태로 반환하여 JSON 형식으로 응답
     *
     * ## 기술
     * - `Principal` 객체에서 로그인한 사용자의 ID를 가져옴
     * - `memberRepository`를 통해 사용자의 고유 번호(`memNo`)를 조회하고, 이를 통해 읽지 않은 공지사항 목록을 `noticeService`로부터 가져옴
     * - 조회한 데이터를 `NoticeStatusDto` 형태로 변환 후 응답으로 반환
     *
     * @param principal 현재 인증된 사용자의 인증 정보
     * @return ResponseEntity<List<NoticeStatusDto>> - 읽지 않은 공지사항 목록의 DTO 리스트를 포함한 응답
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NoticeStatusDto>> getUnreadNotices(Principal principal) {
        // Principal에서 사용자의 아이디(memId) 가져오기
        String memId = principal.getName();

        // memId로 Member 조회
        Member member = memberRepository.findByMemId(memId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        Long memNo = member.getMemNo();  // memNo 가져오기

        // 읽지 않은 공지사항 목록 조회
        List<NoticeStatusDto> unreadNotices = noticeService.getUnreadNoticesForMember(memNo)
                .stream()
                .map(NoticeStatusDto::fromEntity)
                .toList();
        
        return ResponseEntity.ok(unreadNotices);
    }

    /**
     * 공지사항 알림 데이터를 반환하는 API 메서드.
     *
     * ## 기능
     * - 현재 로그인한 사용자의 ID(`memId`)를 통해 해당 사용자가 읽지 않은 공지사항이 있는지 여부를 확인
     * - 공지사항 알림 여부 데이터를 JSON 형식으로 반환
     *
     * ## 기술
     * - `Principal` 객체를 통해 로그인한 사용자의 ID를 가져옴
     * - `memberRepository`를 통해 사용자 정보를 조회하여 회원 번호(`memNo`)를 얻음
     * - `noticeService`의 `hasUnreadNotices` 메서드를 호출해 해당 회원의 읽지 않은 공지사항 여부를 확인
     * - 결과 데이터를 `Map` 형태로 반환하여 클라이언트에서 알림 표시 여부를 확인할 수 있도록 함
     *
     * @param principal 현재 인증된 사용자의 인증 정보
     * @return ResponseEntity<Map<String, Boolean>> - 읽지 않은 공지사항 여부 데이터를 포함한 응답
     */
    @GetMapping("/nav/notice-notifications")
    public ResponseEntity<Map<String, Boolean>> getNoticeNotifications(Principal principal) {
        String username = principal.getName();
        Optional<Member> memberOpt = memberRepository.findByMemId(username);

        Map<String, Boolean> notifications = new HashMap<>();

        if (memberOpt.isPresent()) {
            Long memNo = memberOpt.get().getMemNo();
            boolean hasUnreadNotices = noticeService.hasUnreadNotices(memNo);
            notifications.put("hasUnreadNotices", hasUnreadNotices);
        }

        return ResponseEntity.ok(notifications);
    }
    
}
