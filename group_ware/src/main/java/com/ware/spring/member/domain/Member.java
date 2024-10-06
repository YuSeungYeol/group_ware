package com.ware.spring.member.domain;

import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@ToString
@Table(name="member")
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="mem_no")
    private Long memNo;

    @Column(name="mem_id")
    private String memId;

    @Column(name="mem_pw")
    private String memPw;

    @Column(name="mem_name")
    private String memName;

    @Column(name="mem_phone")
    private String memPhone;

    @Column(name="mem_email")
    private String memEmail;

    @Column(name="profile_saved")
    private String profileSaved;    // 저장된 파일명

    @Column(name="mem_reg_date")
    @CreationTimestamp
    private LocalDate memRegDate;

    @Column(name="mem_mod_date")
    @UpdateTimestamp
    private LocalDate memModDate;

    @Column(name="mem_off")
    private double memOff;

    @Column(name="mem_use_off")
    private double memUseOff;

    @ManyToOne
    @JoinColumn(name = "rank_no")
    private Rank rank;

    @ManyToOne
    @JoinColumn(name = "distributor_no")
    private Distributor distributor;

    @Column(name="emp_no")
    private String empNo;  // 사원번호 추가
    
    @Column(name = "mem_leave")
    private String memLeave;
}
