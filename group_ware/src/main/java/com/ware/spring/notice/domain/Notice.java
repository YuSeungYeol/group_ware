package com.ware.spring.notice.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

@Table(name="notice")
@Entity
@Getter
@Setter
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Builder
public class Notice {

	@Id
	@Column(name="notice_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long noticeNo;
	
	@Column(name="notice_title")
	private String noticeTitle;
	
	@Column(name="notice_content")
	private String noticeContent;
	
	@ManyToOne
	@JoinColumn(name="mem_no")
	private Member member;
	
	@Column(name="notice_reg_date")
	@CreationTimestamp
	private LocalDateTime noticeRegDate;
	
	@Column(name="notice_new_date")
	@UpdateTimestamp
	private LocalDateTime noticeNewDate;
	
	@Column(name="notice_view")
	private int noticeView;
	
	@Column(name="deleteYn")
	private String deleteYn;
	
	@Column(name="notice_start_date")
	private LocalDate noticeStartDate;
	
	@Column(name="notice_end_date")
	private LocalDate noticeEndDate;
	
	@Column(name="notice_schedule")
	private String noticeSchedule;
}
