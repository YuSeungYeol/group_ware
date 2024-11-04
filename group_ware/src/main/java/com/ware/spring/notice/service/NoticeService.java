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
    
    /**
     * 공지사항 목록을 조회하는 메서드.
     *
     * ## 기능
     * - 주어진 검색 조건(`NoticeDto`)에 따라 공지사항 목록을 조회
     * - 검색 조건으로 제목(`noticeTitle`)이 주어지면 해당 제목을 포함하는 공지사항만 조회
     * - 제목이 주어지지 않으면 전체 공지사항 목록을 반환
     *
     * ## 기술
     * - `NoticeDto` 객체에서 제목을 가져와 검색 조건을 확인
     * - 제목이 존재할 경우, `noticeRepository`의 `findBynoticeTitleContaining` 메서드를 호출하여 제목에 해당하는 공지사항을 페이지로 반환
     * - 제목이 없을 경우, 전체 공지사항 목록을 페이지로 반환
     *
     * @param searchDto 공지사항 검색 조건을 담고 있는 DTO 객체
     * @param pageable 페이지 요청 정보
     * @return Page<Notice> - 조회된 공지사항 목록을 포함한 페이지 객체
     */
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
    /**
     * 공지사항을 등록하는 메서드.
     *
     * ## 기능
     * - 주어진 `NoticeDto` 객체를 사용하여 새로운 공지사항을 생성하고 저장
     * - 공지사항의 작성자는 `Member` 객체를 통해 설정
     * - 공지사항의 일정 여부(`noticeSchedule`)에 따라 시작일과 종료일을 설정
     *
     * ## 기술
     * - `NoticeDto` 객체의 데이터를 `Notice` 엔티티로 변환하여 저장
     * - 일정 여부가 null인 경우, 기본값으로 "N"을 설정
     * - 일정이 "Y"인 경우, 시작일과 종료일을 설정하며, 그렇지 않으면 해당 필드를 null로 설정
     *
     * @param dto 공지사항 정보를 담고 있는 DTO 객체
     * @param member 공지사항 작성자 정보가 담긴 Member 객체
     * @return Notice - 저장된 공지사항 엔티티
     */
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
   
    /**
     * 특정 공지사항의 상세 정보를 조회하는 메서드.
     *
     * ## 기능
     * - 공지사항 번호(`notice_no`)를 사용하여 해당 공지사항을 데이터베이스에서 조회
     * - 조회한 공지사항 엔티티를 `NoticeDto` 객체로 변환하여 반환
     *
     * ## 기술
     * - `noticeRepository`를 통해 공지사항 엔티티를 조회
     * - 조회된 엔티티를 `NoticeDto`로 변환하는 과정에서 데이터 전송 객체의 필드에 매핑
     * 
     * @param notice_no 조회할 공지사항의 번호
     * @return NoticeDto - 조회된 공지사항의 정보가 담긴 DTO 객체
     * @throws RuntimeException 공지사항이 존재하지 않을 경우
     */
    public NoticeDto selectNoticeOne(Long notice_no) {
    		Notice origin = noticeRepository.findByNoticeNo(notice_no);
    		NoticeDto dto = new NoticeDto().toDto(origin);
    	return dto;
    	}
    
    /**
     * 특정 공지사항의 조회수를 증가시키는 메서드.
     *
     * ## 기능
     * - 공지사항 번호(`noticeNo`)를 사용하여 해당 공지사항을 데이터베이스에서 조회
     * - 조회수(`noticeView`)를 1 증가시킴
     * - 변경된 공지사항 정보를 데이터베이스에 저장
     *
     * ## 기술
     * - `noticeRepository`를 통해 공지사항 엔티티를 조회
     * - 조회된 엔티티의 조회수 필드를 업데이트하고, 다시 저장
     * 
     * @param noticeNo 조회수를 증가시킬 공지사항의 번호
     * @throws RuntimeException 공지사항이 존재하지 않을 경우
     */
    public void increaseViewCount(Long noticeNo) {
    		Notice notice = noticeRepository.findById(noticeNo).orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
    		notice.setNoticeView(notice.getNoticeView() + 1);
        noticeRepository.save(notice);
    }
    
    /**
     * 공지사항을 수정하는 메서드.
     *
     * ## 기능
     * - 수정할 공지사항의 번호(`noticeNo`)로 해당 공지사항을 조회
     * - 수정된 제목과 내용을 새로운 DTO 값으로 업데이트
     * - 변경된 공지사항 정보를 데이터베이스에 저장
     *
     * ## 기술
     * - `selectNoticeOne` 메서드를 통해 기존 공지사항 DTO를 가져옴
     * - DTO에서 제목과 내용을 수정하고, 다시 공지사항 엔티티로 변환
     * - 수정된 공지사항 엔티티를 데이터베이스에 저장
     * 
     * @param dto 수정할 공지사항의 정보가 담긴 DTO
     * @return 수정된 공지사항 엔티티
     * @throws RuntimeException 공지사항이 존재하지 않을 경우
     */
    public Notice updateNotice(NoticeDto dto) {
        NoticeDto temp = selectNoticeOne(dto.getNoticeNo());
        temp.setNoticeTitle(dto.getNoticeTitle());
        temp.setNoticeContent(dto.getNoticeContent());
        
        Notice notice = temp.toEntity();
        Notice result = noticeRepository.save(notice);
        return result;
    }
    
    /**
     * 공지사항을 삭제하는 메서드.
     *
     * ## 기능
     * - 주어진 공지사항 번호(`notice_no`)를 사용하여 해당 공지사항을 데이터베이스에서 삭제
     * - 삭제가 성공하면 1을 반환하고, 실패 시에는 0을 반환
     *
     * ## 기술
     * - `noticeRepository`의 `deleteById` 메서드를 사용하여 공지사항 삭제 시도
     * - 예외가 발생할 경우 예외 정보를 출력
     * 
     * @param notice_no 삭제할 공지사항의 번호
     * @return 삭제 성공 시 1, 실패 시 0
     */
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
    
    /**
     * 모든 직원에게 공지사항 알림을 설정하는 메서드.
     *
     * ## 기능
     * - 새로운 공지사항이 생성되면 모든 직원(Member)에게 해당 공지사항에 대한 상태(읽음 여부)를 저장
     * - 각 직원의 공지사항 상태는 "읽지 않음"으로 초기화
     *
     * ## 기술
     * - `memberRepository`를 사용하여 모든 직원을 조회
     * - 각 직원에 대해 `NoticeStatus` 객체를 생성하고 이를 데이터베이스에 저장
     * - 트랜잭션 관리가 필요하므로 `@Transactional` 어노테이션 사용
     * 
     * @param notice 저장할 공지사항 객체
     */
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

    /**
     * 특정 회원의 읽지 않은 공지사항 목록을 조회하는 메서드.
     *
     * ## 기능
     * - 주어진 회원 번호(memNo)에 해당하는 회원이 읽지 않은 공지사항 상태를 반환
     *
     * ## 기술
     * - `noticeStatusRepository`를 사용하여 특정 회원의 공지사항 상태를 조회
     * - 읽지 않은 상태는 "N"으로 표시됨
     * 
     * @param memNo 읽지 않은 공지사항을 조회할 회원의 번호
     * @return 읽지 않은 공지사항의 리스트
     */
    public List<NoticeStatus> getUnreadNoticesForMember(Long memNo) {
        return noticeStatusRepository.findByMember_MemNoAndIsRead(memNo, "N");
    }
    
    /**
     * 특정 회원의 읽지 않은 공지사항이 있는지 확인하는 메서드.
     *
     * ## 기능
     * - 주어진 회원 번호(memNo)에 해당하는 회원이 읽지 않은 공지사항이 존재하는지 여부를 반환
     *
     * ## 기술
     * - `noticeStatusRepository`를 사용하여 특정 회원의 읽지 않은 공지사항 존재 여부를 확인
     * - 읽지 않은 상태는 "N"으로 표시됨
     * 
     * @param memNo 읽지 않은 공지사항을 확인할 회원의 번호
     * @return 읽지 않은 공지사항이 존재하면 true, 그렇지 않으면 false
     */
    public boolean hasUnreadNotices(Long memNo) {
        // NoticeStatusRepository를 사용하여 해당 사용자가 읽지 않은 공지사항이 있는지 확인
        return noticeStatusRepository.existsByMember_MemNoAndIsRead(memNo, "N");
    }
    
    /**
     * 특정 공지사항을 읽음 상태로 업데이트하는 메서드.
     *
     * ## 기능
     * - 지정된 공지사항(noticeNo)과 회원(memNo)에 대해 읽음 상태를 "Y"로 설정
     * 
     * ## 기술
     * - `noticeStatusRepository`를 사용하여 해당 공지사항과 회원에 대한 현재 상태를 조회
     * - 읽음 상태로 업데이트가 성공하면 해당 상태를 저장
     *
     * @param noticeNo 읽음 상태로 변경할 공지사항의 번호
     * @param memNo 공지사항을 읽은 회원의 번호
     */
    @Transactional
    public void markNoticeAsRead(Long noticeNo, Long memNo) {
        Optional<NoticeStatus> noticeStatusOpt = noticeStatusRepository.findByNotice_NoticeNoAndMember_MemNo(noticeNo, memNo);
        if (noticeStatusOpt.isPresent()) {
            NoticeStatus noticeStatus = noticeStatusOpt.get();
            noticeStatus.setIsRead("Y");
            noticeStatusRepository.save(noticeStatus);
        }
    }
    
    /**
     * 특정 공지사항을 공지사항 번호로 조회하는 메서드.
     *
     * ## 기능
     * - 주어진 공지사항 번호(noticeNo)에 해당하는 공지사항을 데이터베이스에서 조회
     *
     * ## 기술
     * - `noticeRepository`를 사용하여 공지사항 번호에 해당하는 공지사항 객체를 반환
     *
     * @param noticeNo 조회할 공지사항의 번호
     * @return 공지사항 객체. 존재하지 않을 경우 null을 반환
     */
    public Notice findByNoticeNo(Long noticeNo) {
    	return noticeRepository.findByNoticeNo(noticeNo);
    }

}



