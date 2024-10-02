package com.ware.spring.authLate.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auth_late")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
public class AuthLate {

    @Id
    @Column(name = "author_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authorNo;

    @ManyToOne
    @JoinColumn(name = "mem_no")
    private Member member;

    @Column(name = "late_title")
    private String lateTitle;

    @Column(name = "late_content")
    private String lateContent;

    @Column(name = "late_status")
    private String lateStatus;

    @Column(name = "late_reg_date")
    @CreationTimestamp
    private LocalDateTime lateRegDate;

    @Column(name = "late_mod_date")
    @UpdateTimestamp
    private LocalDateTime lateModDate;

    @OneToOne
    @JoinColumn(name = "author_no", referencedColumnName = "author_no", insertable = false, updatable = false)
    private Authorization authorization;  // `Authorization` 테이블과의 관계
}
