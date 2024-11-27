package com.ware.spring.commute.domain;

import java.sql.Time;
import java.time.LocalDateTime;

import com.ware.spring.member.domain.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommuteDto {

    private Long commuteNo;
    private Member member;  // Member 객체를 사용
    private LocalDateTime commuteOnStartTime;
    private LocalDateTime commuteOnEndTime;
    private String commuteFlagBlue;
    private String commuteFlagPurple;
    private Time commuteOutTime;
    private String isLate;  // 지각 여부 (Y/N)

    
    // 엔티티로 변환메서드
    public Commute toEntity() {
        return Commute.builder()
                .commuteNo(this.commuteNo)
                .member(this.member)  // Member 객체를 엔티티로 전달
                .commuteOnStartTime(this.commuteOnStartTime)
                .commuteOnEndTime(this.commuteOnEndTime)
                .commuteFlagBlue(this.commuteFlagBlue)
                .commuteFlagPurple(this.commuteFlagPurple)
                .commuteOutTime(this.commuteOutTime)
                .isLate(this.isLate)
                .build();
    }

    // Dto로 변환하는 메서드
    public static CommuteDto toDto(Commute commute) {
        return CommuteDto.builder()
                .commuteNo(commute.getCommuteNo())
                .member(commute.getMember())  // Member 객체를 포함
                .commuteOnStartTime(commute.getCommuteOnStartTime())
                .commuteOnEndTime(commute.getCommuteOnEndTime())
                .commuteFlagBlue(commute.getCommuteFlagBlue())
                .commuteFlagPurple(commute.getCommuteFlagPurple())
                .commuteOutTime(commute.getCommuteOutTime())
                .isLate(commute.getIsLate())
                .build();
    }
}
