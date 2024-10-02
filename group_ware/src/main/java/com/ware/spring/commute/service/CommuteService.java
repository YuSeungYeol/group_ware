package com.ware.spring.commute.service;

import com.ware.spring.commute.domain.Commute;
import com.ware.spring.commute.repository.CommuteRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CommuteService {

    private final CommuteRepository commuteRepository;
    private final MemberRepository memberRepository;

    public CommuteService(CommuteRepository commuteRepository, MemberRepository memberRepository) {
        this.commuteRepository = commuteRepository;
        this.memberRepository = memberRepository;
    }

    // 오늘 출근 기록 여부 확인
    public boolean hasTodayCommute(Long memNo) {
        Member member = memberRepository.findById(memNo)
            .orElse(null);  // null 처리
        if (member == null) {
            // 로그로 오류 기록
            System.out.println("존재하지 않는 회원입니다: " + memNo);
            return false;
        }

        Optional<Commute> todayCommute = commuteRepository.findTodayCommuteByMember(member);
        return todayCommute.isPresent();
    }

    // 출근 기록
    public Commute startWork(Long memNo) {
        // memNo로 Member 객체 조회
        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 오늘 해당 회원의 출근 기록이 있는지 확인
        Optional<Commute> existingCommute = commuteRepository.findTodayCommuteByMember(member);

        if (existingCommute.isPresent()) {
            return existingCommute.get();  // 이미 출근 기록이 있으면 해당 기록을 반환
        } else {
            // 새로운 출근 기록 생성
            Commute commute = Commute.builder()
                    .member(member)
                    .commuteOnStartTime(LocalDateTime.now())
                    .commuteFlagBlue("Y")
                    .commuteFlagPurple("N")
                    .build();
            return commuteRepository.save(commute);
        }
    }

    // 퇴근 기록 및 근무 시간 계산
    public Map<String, Object> endWork(Long memNo) {
        // memNo로 Member 객체 조회
        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Optional<Commute> commuteOpt = commuteRepository.findTodayCommuteByMember(member);
        if (commuteOpt.isPresent()) {
            Commute commute = commuteOpt.get();
            LocalDateTime endTime = LocalDateTime.now();
            commute.setCommuteOnEndTime(endTime);
            commute.setCommuteFlagBlue("N");
            commute.setCommuteFlagPurple("N");
            commuteRepository.save(commute);

            // 출근 시간과 퇴근 시간의 차이를 계산
            LocalDateTime startTime = commute.getCommuteOnStartTime();
            long hoursWorked = Duration.between(startTime, endTime).toHours();
            long minutesWorked = Duration.between(startTime, endTime).toMinutes() % 60;

            // 결과를 Map으로 반환
            Map<String, Object> result = new HashMap<>();
            result.put("hoursWorked", hoursWorked);
            result.put("minutesWorked", minutesWorked);
            result.put("startTime", startTime);
            result.put("endTime", endTime);

            return result;
        } else {
            throw new IllegalStateException("출근 기록이 존재하지 않습니다.");
        }
    }

    // 상태 업데이트 메서드 (착석, 외출, 외근, 식사)
    public void updateStatus(Long memNo, String status) {
        // memNo로 Member 객체 조회
        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Optional<Commute> commuteOpt = commuteRepository.findTodayCommuteByMember(member);
        if (commuteOpt.isPresent()) {
            Commute commute = commuteOpt.get();
            if ("seated".equals(status)) {
                commute.setCommuteFlagBlue("Y");
                commute.setCommuteFlagPurple("N");
            } else {
                commute.setCommuteFlagBlue("N");
                commute.setCommuteFlagPurple("Y");
            }
            commuteRepository.save(commute);
        } else {
            throw new IllegalStateException("출근 기록이 존재하지 않습니다.");
        }
    }

    // 퇴근 시 플래그 상태 업데이트
    public void updateEndStatus(Long memNo) {
        // memNo로 Member 객체 조회
        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Optional<Commute> commuteOpt = commuteRepository.findTodayCommuteByMember(member);
        if (commuteOpt.isPresent()) {
            Commute commute = commuteOpt.get();
            commute.setCommuteFlagBlue("N");
            commute.setCommuteFlagPurple("N");
            commuteRepository.save(commute);
        }
    }
}
