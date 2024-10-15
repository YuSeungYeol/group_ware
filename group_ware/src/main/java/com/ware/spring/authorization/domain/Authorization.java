package com.ware.spring.authorization.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ware.spring.authTrip.domain.AuthTrip;
import com.ware.spring.authOff.domain.AuthOff;
import com.ware.spring.authOutside.domain.AuthOutside;
import com.ware.spring.authOvertime.domain.AuthOvertime;
import com.ware.spring.authLate.domain.AuthLate;
import com.ware.spring.approval_route.domain.ApprovalRoute;
import com.ware.spring.member.domain.Member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Entity
@Table(name = "authorization")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
public class Authorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_no")
    private Long authorNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mem_no")
    private Member member;  // 전자결재 요청한 직원

    @Column(name = "emp_no")
    private String empNo;
    
    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_status")
    private String authorStatus; // 전자결재 상태 (P: 대기, Y: 승인, N: 반려, T: 임시 저장)
    
    @Column(name="author_reg_date")
    @CreationTimestamp
    private LocalDateTime authorRegDate;
    
    @Column(name="author_mod_date")
    @UpdateTimestamp
    private LocalDateTime authorModDate;

    @Column(name = "author_ori_thumbnail")
    private String authorOriThumbnail;
    
    @Column(name="author_new_thumbnail")
    private String authorNewThumbnail;

    @OneToOne(mappedBy = "authorization", cascade = CascadeType.ALL)
    private AuthTrip authTrip;  // 해외출장 문서 테이블과의 관계

    @OneToOne(mappedBy = "authorization", cascade = CascadeType.ALL)
    private AuthOff authOff;  // 휴가결재 문서 테이블과의 관계

    @OneToOne(mappedBy = "authorization", cascade = CascadeType.ALL)
    private AuthLate authLate;  // 지각사유서 문서 테이블과의 관계
    
    @OneToOne(mappedBy = "authorization", cascade = CascadeType.ALL)
    private AuthOutside authOutside;  // 외근 문서 테이블과의 관계
    
    @OneToOne(mappedBy = "authorization", cascade = CascadeType.ALL)
    private AuthOvertime authOvertime;  // 야근 문서 테이블과의 관계

    @Column(name="auth_title")
    private String authTitle;
    
    @Column(name="auth_content")
    private String authContent;
    
    @Column(name = "folder_private_no")
    private Integer folderPrivateNo;

    // ApprovalRoute와의 일대다 관계 설정 (양방향)
    @OneToMany(mappedBy = "authorization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApprovalRoute> approvalRoutes;

    @Column(name="doctype")
    private String doctype;

    @Column(name="signature")
    private String signature;
    
    // 새로 추가된 필드
    @Column(name = "leaveType")
    private String leaveType;    // 휴가 구분

    @Column(name = "startDate")
    private String startDate;    // 시작 일정, 지각 일시, 해외 출장 시작 일정, 외근 시작 일시, 야근 시작 일시

    @Column(name = "endDate")
    private String endDate;      // 종료 일정, 해외 출장 종료 일정, 외근 종료 일시, 야근 종료 일시

    @Column(name = "startEndDate")
    private Double startEndDate; // 기간이나 일수

    @Column(name = "lateType")
    private String lateType;     // 지각 사유

    @Column(name = "tripType")
    private String tripType;     // 출장 구분

    @Column(name = "outsideType")
    private String outsideType;  // 외근 구분

    @Column(name = "overtimeType")
    private String overtimeType; // 야근 구분

}
