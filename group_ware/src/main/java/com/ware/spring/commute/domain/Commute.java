package com.ware.spring.commute.domain;

import com.ware.spring.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "commute")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commuteNo;

    @ManyToOne  // Member 엔티티와 연관관계 설정
    @JoinColumn(name = "mem_no")
    private Member member;

    private LocalDateTime commuteOnStartTime;
    private LocalDateTime commuteOnEndTime;
    private String commuteFlagBlue;
    private String commuteFlagPurple;
    private LocalDateTime commuteOutTime;

    // 추가 메서드들이 있을 수 있음
}
