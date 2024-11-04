package com.ware.spring.notice.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.notice.service.NoticeService;

import jakarta.servlet.http.HttpSession;

import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.notice.domain.Notice;
import com.ware.spring.notice.domain.NoticeDto;


@Controller
public class NoticeApiController {

    private final NoticeService noticeService;
    private final MemberRepository memberRepository;

    @Autowired
    public NoticeApiController(NoticeService noticeService, MemberRepository memberRepository) {
        this.noticeService = noticeService;
        this.memberRepository = memberRepository;
    }

    /**
     * 공지사항을 등록하는 메서드.
     *
     * ## 기능
     * - 로그인한 사용자의 정보를 기반으로 공지사항을 등록
     * - 등록 권한 확인 (회원의 직급이 6 이상인 경우에만 등록 가능)
     * - 공지사항 일정이 설정된 경우 시작일과 종료일 검증
     * - 공지사항 등록 성공 시 모든 사용자에게 공지사항 알림 추가
     *
     * ## 기술
     * - Spring Security `Principal`과 `HttpSession`을 이용해 로그인된 사용자 정보 확인
     * - 공지사항 등록 권한은 회원의 `RankNo` 값으로 결정
     * - JPA를 통해 공지사항을 저장하고, 모든 사용자에게 알림 정보를 추가하는 서비스 호출
     *
     * @param dto 등록할 공지사항의 DTO
     * @param principal 현재 로그인한 사용자의 인증 객체 (Spring Security 사용)
     * @param session 사용자 세션 객체 (로그인 정보가 없는 경우 사용)
     * @return Map<String, String> - 응답 코드와 메시지를 담은 결과 맵
     */
    @ResponseBody
    @PostMapping("/notice")
    public Map<String, String> createNotice(NoticeDto dto, Principal principal, HttpSession session) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "게시글 등록 중 오류가 발생했습니다.");

        // 로그인한 사용자 정보 가져오기
        String username = principal != null ? principal.getName() : ((Member) session.getAttribute("loggedInUser")).getMemId();
        Member loggedInMember = memberRepository.findByMemId(username)
            .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));

        if (loggedInMember.getRank().getRankNo() < 6) {
            resultMap.put("res_msg", "공지사항 등록 권한이 없습니다.");
            return resultMap;
        }

        // dto에 Member 객체 설정
        dto.setMember(loggedInMember);
        dto.setNoticeView(0);  // 조회수 기본값 설정

        // 체크박스 값이 'Y'인지 확인
        if ("Y".equals(dto.getNoticeSchedule())) {
            if (dto.getNoticeStartDate() == null || dto.getNoticeEndDate() == null) {
                resultMap.put("res_msg", "공지 시작일과 종료일을 입력하세요.");
                return resultMap;
            }
        } else {   
            dto.setNoticeStartDate(null);
            dto.setNoticeEndDate(null);
        }

        Notice createdNotice = noticeService.createNotice(dto, loggedInMember);

        if (createdNotice != null) {
            System.out.println("공지사항이 성공적으로 생성되었습니다: " + createdNotice.getNoticeNo());
            
            // 공지사항 상태 저장: 모든 사용자에게 알림 추가
            noticeService.createNoticeForAllMembers(createdNotice);
            
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "게시글이 성공적으로 등록되었습니다.");
        }

        return resultMap;
    }

    /**
     * 공지사항을 수정하는 메서드.
     *
     * ## 기능
     * - 로그인한 사용자의 정보를 바탕으로 공지사항 수정 권한을 확인
     * - 공지사항의 작성자가 아닌 경우 수정이 불가능하며, 권한이 없음을 알림
     * - 입력된 내용으로 제목과 내용을 업데이트하여 수정
     *
     * ## 기술
     * - Spring Security `Principal`을 통해 로그인된 사용자 정보를 가져옴
     * - JPA를 이용하여 작성자 정보를 확인하고, 작성자가 일치할 경우에만 수정 가능
     * - 성공적으로 수정된 경우 HTTP 응답 코드 200을 반환
     *
     * @param dto 수정할 공지사항의 DTO
     * @param principal 현재 로그인한 사용자의 인증 객체 (Spring Security 사용)
     * @return Map<String, String> - 응답 코드와 메시지를 담은 결과 맵
     */
    @ResponseBody
    @PostMapping("/notice/{notice_no}")
    public Map<String, String> updateNotice(NoticeDto dto, Principal principal) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "게시글 수정 중 오류가 발생했습니다.");

        // 로그인한 사용자의 memNo 가져오기
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));
        
        // 수정할 게시글의 작성자 확인
        NoticeDto originalDto = noticeService.selectNoticeOne(dto.getNoticeNo());
        if (!originalDto.getMember().getMemNo().equals(loggedInMember.getMemNo())) {
            resultMap.put("res_msg", "본인이 작성한 글만 수정할 수 있습니다.");
            return resultMap;
        }

        // 제목과 내용 업데이트
        if (noticeService.updateNotice(dto) != null) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "게시글이 성공적으로 수정되었습니다.");
        }

        return resultMap;
    }

    /**
     * 공지사항을 삭제하는 메서드.
     *
     * ## 기능
     * - 로그인한 사용자의 정보를 바탕으로 공지사항 삭제 권한을 확인
     * - 공지사항의 작성자가 아닌 경우 삭제가 불가능하며, 권한이 없음을 알림
     * - 삭제가 성공적으로 이루어진 경우 HTTP 응답 코드 200을 반환
     *
     * ## 기술
     * - Spring Security `Principal`을 통해 로그인된 사용자 정보를 가져옴
     * - JPA를 이용하여 작성자 정보를 확인하고, 작성자가 일치할 경우에만 삭제 가능
     * - 삭제 과정에서 JPA 예외 발생 시 404 오류 메시지를 반환하여 처리 실패 알림
     *
     * @param notice_no 삭제할 공지사항의 고유 번호
     * @param principal 현재 로그인한 사용자의 인증 객체 (Spring Security 사용)
     * @return Map<String, String> - 응답 코드와 메시지를 담은 결과 맵
     */
    @ResponseBody
    @DeleteMapping("/notice/{notice_no}")
    public Map<String, String> deleteNotice(@PathVariable("notice_no") Long notice_no, Principal principal) {
        Map<String, String> map = new HashMap<>();
        map.put("res_code", "404");
        map.put("res_msg", "게시글 삭제 중 오류가 발생했습니다.");

        // 로그인한 사용자 정보 가져오기
        String username = principal.getName();
        Member loggedInMember = memberRepository.findByMemId(username)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));
 
        // 삭제할 게시글의 작성자 확인
        NoticeDto originalDto = noticeService.selectNoticeOne(notice_no);
        if (!originalDto.getMember().getMemNo().equals(loggedInMember.getMemNo())) {
            map.put("res_msg", "본인이 작성한 글만 삭제할 수 있습니다.");
            return map;
        }

        // 실제 삭제
        if (noticeService.deleteNotice(notice_no) > 0) {
            map.put("res_code", "200");
            map.put("res_msg", "정상적으로 게시글이 삭제되었습니다.");
        }

        return map;
    }
    
    /**
     * 공지사항 알림을 읽음 처리하는 메서드.
     *
     * ## 기능
     * - 현재 로그인한 사용자의 ID와 공지사항 번호를 기반으로 해당 공지사항 알림을 읽음 처리
     * - 사용자가 로그인되어 있지 않거나 존재하지 않는 경우, 401 Unauthorized 상태 반환
     *
     * ## 기술
     * - Spring Security `Principal`을 통해 로그인된 사용자 정보 가져오기
     * - JPA를 통해 사용자가 존재하는지 확인한 후, 공지사항 알림을 읽음 처리
     * - 성공 시 200 OK 상태를 반환하고, 실패 시 401 Unauthorized 상태 반환
     *
     * @param noticeNo 읽음 처리할 공지사항의 고유 번호
     * @param principal 현재 로그인한 사용자의 인증 객체 (Spring Security 사용)
     * @return ResponseEntity<Void> - HTTP 상태 코드만 반환
     */
    @PostMapping("/clearNoticeNotification/{noticeNo}")
    public ResponseEntity<Void> clearNoticeNotification(@PathVariable("noticeNo") Long noticeNo, Principal principal) {
        String username = principal.getName();
        Optional<Member> memberOpt = memberRepository.findByMemId(username);

        if (memberOpt.isPresent()) {
            Long memNo = memberOpt.get().getMemNo();
            noticeService.markNoticeAsRead(noticeNo, memNo);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }



    


}

