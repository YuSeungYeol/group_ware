package com.ware.spring.authOff.domain;

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
@Table(name = "auth_off")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
public class AuthOff {

    @Id
    @Column(name = "author_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authorNo;

    @ManyToOne
    @JoinColumn(name = "mem_no")
    private Member member;

    @Column(name = "off_title")
    private String offTitle;

    @Column(name = "off_content")
    private String offContent;

    @Column(name = "off_status")
    private String offStatus;

    @Column(name = "off_reg_date")
    @CreationTimestamp
    private LocalDateTime offRegDate;

    @Column(name = "off_mod_date")
    @UpdateTimestamp
    private LocalDateTime offModDate;

    @OneToOne
    @JoinColumn(name = "author_no", referencedColumnName = "author_no", insertable = false, updatable = false)
    private Authorization authorization;  // `Authorization` 테이블과의 관계

}
