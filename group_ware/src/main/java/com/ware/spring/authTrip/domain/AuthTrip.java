package com.ware.spring.authTrip.domain;

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
@Table(name = "auth_trip")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
public class AuthTrip {

    @Id
    @Column(name = "author_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authorNo;

    @ManyToOne
    @JoinColumn(name = "mem_no")
    private Member member;

    @Column(name = "trip_title")
    private String tripTitle;

    @Column(name = "trip_content")
    private String tripContent;

    @Column(name = "trip_status")
    private String tripStatus;

    @Column(name = "trip_reg_date")
    @CreationTimestamp
    private LocalDateTime tripRegDate;

    @Column(name = "trip_mod_date")
    @UpdateTimestamp
    private LocalDateTime tripModDate;

    @OneToOne
    @JoinColumn(name = "author_no", referencedColumnName = "author_no", insertable = false, updatable = false)
    private Authorization authorization;
}
