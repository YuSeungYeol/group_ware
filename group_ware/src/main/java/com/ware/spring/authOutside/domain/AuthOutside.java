package com.ware.spring.authOutside.domain;

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
@Table(name = "auth_outside")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
public class AuthOutside {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_no")
    private Long authorNo;

    @ManyToOne
    @JoinColumn(name = "mem_no")
    private Member member;

    @Column(name = "outside_title")
    private String outsideTitle;

    @Column(name = "outside_content")
    private String outsideContent;

    @Column(name = "outside_status")
    private String outsideStatus;

    @Column(name = "outside_reg_date")
    @CreationTimestamp
    private LocalDateTime outsideRegDate;

    @Column(name = "outside_mod_date")
    @UpdateTimestamp
    private LocalDateTime outsideModDate;

    @OneToOne
    @JoinColumn(name = "author_no", referencedColumnName = "author_no", insertable = false, updatable = false)
    private Authorization authorization;  // `Authorization` 테이블과의 관계
}
