package com.ware.spring.commute.domain;

import java.sql.Time;
import java.time.LocalDateTime;

import com.ware.spring.member.domain.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Time commuteOutTime;
    private String isLate;  // 지각 여부 (Y/N)

    // 추가 메서드들이 있을 수 있음
}
