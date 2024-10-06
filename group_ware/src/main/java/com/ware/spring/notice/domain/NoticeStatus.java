package com.ware.spring.notice.domain;

import com.ware.spring.member.domain.Member;

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

@Table(name="notice_status")
@Entity
@Getter
@Setter
@NoArgsConstructor(access=AccessLevel.PUBLIC)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Builder
public class NoticeStatus {

	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 @Column(name="notice_status_no")
	 private Long noticeStatusNo;
	 
	 @ManyToOne
	 @JoinColumn(name="notice_no")
	 private Notice notice;

	 @ManyToOne
	 @JoinColumn(name="mem_no")
	 private Member member;

	 @Column(name="is_read")
	 private String isRead; 
		
		
}
