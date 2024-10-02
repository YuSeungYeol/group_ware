package com.ware.spring.board.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.Member;
import com.ware.spring.board.domain.Board;

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

@Table(name="board")
@Entity
@Getter
@Setter
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Builder
public class Board {
	
	@Id
	@Column(name="board_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long boardNo;
	
	@Column(name="board_title")
	private String boardTitle;
	
	@Column(name="board_content")
	private String boardContent;
	
	@ManyToOne
	@JoinColumn(name="mem_no")
	private Member member;
	
	@Column(name="board_reg_date")
	@CreationTimestamp
	private LocalDateTime boardRegDate;
	
	@Column(name="board_new_date")
	@UpdateTimestamp
	private LocalDateTime boardNewDate;
	
	@Column(name="board_view")
	private int boardView;
	
	@Column(name="deleteYn")
	private String deleteYn;
	
}
