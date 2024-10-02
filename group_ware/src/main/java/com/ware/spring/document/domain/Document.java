package com.ware.spring.document.domain;

import java.time.LocalDateTime;

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

@Entity
@Table(name = "document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Document {

	@Id
    @Column(name = "document_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentNo;
	
	@ManyToOne
    @JoinColumn(name = "document_writer")
    private Member member; 
	
	@Column(name="document_title")
	private String documentTitle;
	
	@Column(name="document_content")
	private String documentContent;
	
	@Column(name="document_status")
	private String documentStatus;  
	// 문서 상태 (예: 대기, 작성중, 임시저장, 반려됨)
	
	@ManyToOne
    @JoinColumn(name = "create_by")
    private Member createdBy;  
	
	@Column(name="document_reg_date")
	private LocalDateTime documentRegDate;  

	@Column(name="document_mod_date")
	private LocalDateTime documentModDate;  
	
	@Column(name="document_sort")
	private String documentSort; 
}
