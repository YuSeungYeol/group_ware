package com.ware.spring.notice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ware.spring.member.domain.Member;
import com.ware.spring.notice.domain.Notice;
import com.ware.spring.notice.domain.NoticeDto;
import com.ware.spring.notice.repository.NoticeRepository;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    
    @Autowired
    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
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
        	notice.setMember(member);  // mem_no 값을 설정
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
}



