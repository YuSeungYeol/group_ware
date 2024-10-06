package com.ware.spring.schedule.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ware.spring.notice.domain.Notice;
import com.ware.spring.notice.repository.NoticeRepository;
import com.ware.spring.notice.service.NoticeService;
import com.ware.spring.schedule.domain.Schedule;
import com.ware.spring.schedule.domain.ScheduleDto;
import com.ware.spring.schedule.repository.ScheduleRepository;

@Service
public class ScheduleService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<ScheduleDto> getAllSchedulesAndNotices(String username) {
        // 개인 일정 가져오기
        List<ScheduleDto> schedules = getSchedulesForUser(username);

        // 공지사항 가져오기 (Pageable 없이 전체 공지사항 조회)
        List<Notice> notices = noticeRepository.findAll();

        // 공지사항을 ScheduleDto로 변환하기
        List<ScheduleDto> noticeDtos = notices.stream()
                .map(this::fromNotice)
                .collect(Collectors.toList());

        // 결과 합치기
        List<ScheduleDto> combinedList = new ArrayList<>();
        combinedList.addAll(schedules);
        combinedList.addAll(noticeDtos);

        return combinedList;
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesForUser(String username) {
        return scheduleRepository.findByMemberMemId(username)
                .stream()
                .map(ScheduleDto::toDto)
                .collect(Collectors.toList());
    }
    private ScheduleDto fromNotice(Notice notice) {
        return ScheduleDto.builder()
                .schedule_no(notice.getNoticeNo()) // 공지사항 번호를 가져옴
                .schedule_title(notice.getNoticeTitle())
                .start_date(notice.getNoticeRegDate().toLocalDate())
                .start_time(LocalTime.of(0, 0))
                .end_date(notice.getNoticeRegDate().toLocalDate())
                .end_time(LocalTime.of(23, 59))
                .schedule_background_color("#FFC107")
                .is_notice(true) // 공지사항 플래그 설정
                .schedule_content(notice.getNoticeContent()) // 내용 추가
                .build();
    }

    public void addEmitter(SseEmitter emitter) {
        this.emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter)); // 오류 발생 시 emitter 삭제

        // 주기적으로 연결 유지 메시지 전송
        try {
            emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
        } catch (Exception e) {
            emitters.remove(emitter);
        }
    }

    public void sendNotificationToClients(String message) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("schedule-notification").data(message));
            } catch (Exception e) {
                deadEmitters.add(emitter);
                e.printStackTrace(); // 예외 내용을 출력
            }
        }
        emitters.removeAll(deadEmitters);
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void checkNotificationTimes() {
        LocalDateTime now = LocalDateTime.now();
        List<Schedule> schedules = scheduleRepository.findAll();

        for (Schedule schedule : schedules) {
            if (schedule.getNotification_minutes() != null && schedule.getNotification_minutes() > 0) {
                LocalDateTime notificationTime = schedule.getStart_date().atTime(schedule.getStart_time())
                        .minusMinutes(schedule.getNotification_minutes());

                if (now.isAfter(notificationTime) && now.isBefore(schedule.getStart_date().atTime(schedule.getStart_time()))) {
                    String message = String.format("일정 '%s'이 %d분 후에 시작됩니다.", schedule.getSchedule_title(), schedule.getNotification_minutes());
                    sendNotificationToClients(message);
                    System.out.println("알림 전송됨: " + message); // 알림 전송 로그
                }
            }
        }
    }

    // 기타 기존 메서드들 (생성, 수정, 삭제 등)
    @Transactional
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        Schedule schedule = scheduleDto.toEntity();
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return ScheduleDto.toDto(savedSchedule);
    }

    @Transactional
    public void updateSchedule(Long id, ScheduleDto scheduleDto) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. ID: " + id));
        schedule.update(scheduleDto);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("일정을 찾을 수 없습니다. ID: " + id);
        }
        scheduleRepository.deleteById(id);
    }
}
