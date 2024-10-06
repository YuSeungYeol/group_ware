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
import com.ware.spring.notice.domain.NoticeStatus;
import com.ware.spring.notice.repository.NoticeRepository;
import com.ware.spring.notice.repository.NoticeStatusRepository;

import jakarta.transaction.Transactional;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    private final NoticeStatusRepository noticeStatusRepository; 
    
    @Autowired
    public NoticeService(NoticeRepository noticeRepository
    						,MemberRepository memberRepository
    						,NoticeStatusRepository noticeStatusRepository) {
        this.noticeRepository = noticeRepository;
        this.memberRepository = memberRepository;
        this.noticeStatusRepository = noticeStatusRepository;
        
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

        if (dto.getNoticeSchedule() == null) {
            notice.setNoticeSchedule("N");
        }

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
    
    // 공지사항 등록 후 모든 직원에게 알림 설정
    @Transactional
    public void createNoticeForAllMembers(Notice notice) {
        System.out.println("createNoticeForAllMembers 메서드 실행됨.");
        List<Member> allMembers = memberRepository.findAll();
        for (Member member : allMembers) {
            System.out.println("Member: " + member.getMemNo());
            NoticeStatus noticeStatus = NoticeStatus.builder()
                .notice(notice)
                .member(member)
                .isRead("N")
                .build();
            noticeStatusRepository.save(noticeStatus);
            System.out.println("공지사항 상태 저장 완료: " + member.getMemNo());
        }
    }


    // 특정 회원의 읽지 않은 공지사항 확인
    public List<NoticeStatus> getUnreadNoticesForMember(Long memNo) {
        return noticeStatusRepository.findByMember_MemNoAndIsRead(memNo, "N");
    }
    
    // NoticeService.java에 추가
    public boolean hasUnreadNotices(Long memNo) {
        // NoticeStatusRepository를 사용하여 해당 사용자가 읽지 않은 공지사항이 있는지 확인
        return noticeStatusRepository.existsByMember_MemNoAndIsRead(memNo, "N");
    }
    
    @Transactional
    public void markNoticeAsRead(Long noticeNo, Long memNo) {
        Optional<NoticeStatus> noticeStatusOpt = noticeStatusRepository.findByNotice_NoticeNoAndMember_MemNo(noticeNo, memNo);
        if (noticeStatusOpt.isPresent()) {
            NoticeStatus noticeStatus = noticeStatusOpt.get();
            noticeStatus.setIsRead("Y");
            noticeStatusRepository.save(noticeStatus);
        }
    }
    
    // 캘린더 관련 조회
    public Notice findByNoticeNo(Long noticeNo) {
    	return noticeRepository.findByNoticeNo(noticeNo);
    }

}



