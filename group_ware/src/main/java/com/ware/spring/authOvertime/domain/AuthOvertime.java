package com.ware.spring.authOvertime.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.member.domain.Member;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_overtime")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
public class AuthOvertime {

    @Id
    @Column(name = "author_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authorNo;

    @ManyToOne
    @JoinColumn(name = "mem_no")
    private Member member;

    @Column(name = "overtime_title")
    private String overtimeTitle;

    @Column(name = "overtime_content")
    private String overtimeContent;

    @Column(name = "overtime_status")
    private String overtimeStatus;

    @Column(name = "overtime_reg_date")
    @CreationTimestamp
    private LocalDateTime overtimeRegDate;

    @Column(name = "overtime_mod_date")
    @UpdateTimestamp
    private LocalDateTime overtimeModDate;

    @OneToOne
    @JoinColumn(name = "author_no", referencedColumnName = "author_no", insertable = false, updatable = false)
    private Authorization authorization;  
}
