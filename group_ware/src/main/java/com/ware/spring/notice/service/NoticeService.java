package com.ware.spring.notice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.notice.domain.Notice;
import com.ware.spring.notice.domain.NoticeDto;
import com.ware.spring.notice.repository.NoticeRepository;

import jakarta.transaction.Transactional;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    
    @Autowired
    public NoticeService(NoticeRepository noticeRepository
    						,MemberRepository memberRepository) {
        this.noticeRepository = noticeRepository;
        this.memberRepository = memberRepository;
    }
    
    // Notice 목록을 조회하는 메서드가 데이터를 반환해야 합니다.
    public Page<Notice> selectNoticeList(NoticeDto searchDto, Pageable pageable) {
        String noticeTitle = searchDto.getNoticeTitle();
        if (noticeTitle != null && !noticeTitle.isEmpty()) {
            // 제목 검색
            return noticeRepository.findBynoticeTitleContaining(noticeTitle, pageable);
        } else {
            // 전체 목록 조회
            return noticeRepository.findAll(pageable);
        }
    }
    // 공지사항 등록
    public Notice createNotice(NoticeDto dto, Member member) {
        Notice notice = dto.toEntity();
        notice.setMember(member);

        // noticeSchedule 값이 null이면 기본값을 "N"으로 설정
        if (dto.getNoticeSchedule() == null) {
            notice.setNoticeSchedule("N");
        }

        // 일정이 설정된 경우만 날짜를 설정
        if ("Y".equals(dto.getNoticeSchedule())) {
            notice.setNoticeStartDate(dto.getNoticeStartDate());
            notice.setNoticeEndDate(dto.getNoticeEndDate());
        } else {
            notice.setNoticeStartDate(null);
            notice.setNoticeEndDate(null);
        }

        return noticeRepository.save(notice);
    }   


    // 공지사항 상세화면 
    public NoticeDto selectNoticeOne(Long notice_no) {
    		Notice origin = noticeRepository.findByNoticeNo(notice_no);
    		NoticeDto dto = new NoticeDto().toDto(origin);
    	return dto;
    	}
    // 조회수 증가
    public void increaseViewCount(Long noticeNo) {
    		Notice notice = noticeRepository.findById(noticeNo).orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
    		notice.setNoticeView(notice.getNoticeView() + 1);
        noticeRepository.save(notice);
    }
    // 공지사항 수정
    public Notice updateNotice(NoticeDto dto) {
        NoticeDto temp = selectNoticeOne(dto.getNoticeNo());
        temp.setNoticeTitle(dto.getNoticeTitle());
        temp.setNoticeContent(dto.getNoticeContent());
        
        Notice notice = temp.toEntity();
        Notice result = noticeRepository.save(notice);
        return result;
    }
    // 공지사항 삭제
    public int deleteNotice(Long notice_no) {
		int result = 0;
		try {
			noticeRepository.deleteById(notice_no);
			result = 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
    
    // 공지사항 리스트 조회(삭제 여부 Y,N)
//    public Page<Notice> selectNoticeList(NoticeDto searchDto, Pageable pageable) {
//        String noticeTitle = searchDto.getNoticeTitle();
//        if (noticeTitle != null && !noticeTitle.isEmpty()) {
//            // 제목 검색 및 삭제되지 않은 데이터만 조회
//            return noticeRepository.findByNoticeTitleContainingAndDeleteYn(noticeTitle, "n", pageable);
//        } else {
//            // 삭제되지 않은 전체 목록 조회
//            return noticeRepository.findByDeleteYn("n", pageable);
//        }
//    }
    
//    ALTER TABLE notice ADD COLUMN delete_yn CHAR(1) DEFAULT 'n';
    
//    // 특정 사용자가 읽지 않은 공지사항이 있는지 확인
//    public boolean hasUnreadNotices(Long memNo) {
//        return noticeRepository.existsByMember_MemNoAndNoticeAlram(memNo, "N");
//    }
//
//    // 공지사항 알림 목록 가져오기 (읽지 않은 알림만)
//    public List<Notice> getUnreadNotices(Long memNo) {
//        return noticeRepository.findByMember_MemNoAndNoticeAlram(memNo, "N");
//    }
//
//    // 공지사항 읽음 처리
//    public void clearNoticeNotification(Long noticeNo, Long memNo) {
//        Optional<Notice> noticeOpt = noticeRepository.findByNotice_NoticeNoAndMember_MemNo(noticeNo, memNo);
//        if (noticeOpt.isPresent()) {
//            Notice notice = noticeOpt.get();
//            notice.setNoticeAlram("Y");  // 알림 상태를 'Y'로 변경
//            noticeRepository.save(notice);
//        }
//    }
    
//    // 공지사항 등록 후 모든 직원에게 알림 설정
//    public void createNoticeForAllMembers(Notice notice) {
//        List<Member> allMembers = memberRepository.findAll(); // 모든 회원 조회
//        for (Member member : allMembers) {
//            Notice noticeStatus = new Notice();  // NoticeStatus 객체 생성
//            noticeStatus.setNotice(notice);  // 공지사항 설정
//            noticeStatus.setMember(member);  // 각 회원 설정
//            noticeStatus.setNoticeAlram("N");  // 새로운 공지사항이므로 'N'으로 설정
//            noticeStatusRepository.save(noticeStatus);  // 저장
//        }
//    }
//    
}



