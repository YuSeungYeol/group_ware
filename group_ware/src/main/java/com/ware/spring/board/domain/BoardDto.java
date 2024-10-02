package com.ware.spring.board.domain;

import java.time.LocalDateTime;

import com.ware.spring.member.domain.Member;
import com.ware.spring.board.domain.BoardDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class BoardDto {

	private Long boardNo;
    private String boardTitle;
    private String boardContent;
    private Member member;
    private LocalDateTime boardRegDate;
    private LocalDateTime boardNewDate;
    private int boardView;
    private Long distributorNo;
    
 // search 관련 필드
    private int search_type = 1;
    private String search_text;
    
    // deleteYn 필드 추가
    private String deleteYn;
    
    public Board toEntity() {
    	return Board.builder()
    			.boardNo(boardNo)
                .boardTitle(boardTitle)
                .boardContent(boardContent)
                .boardRegDate(boardRegDate)
                .boardNewDate(boardNewDate)
                .boardView(boardView)
                .member(member)  // Member 객체를 설정
                .deleteYn(deleteYn)  // deleteYn 필드 설정
                .build();
    }
    
    public BoardDto toDto(Board board) {
    	return BoardDto.builder()
    			.boardNo(board.getBoardNo())
                .boardTitle(board.getBoardTitle())
                .boardContent(board.getBoardContent())
                .boardRegDate(board.getBoardRegDate())
                .boardNewDate(board.getBoardNewDate())
                .boardView(board.getBoardView())
                .member(board.getMember())
                .deleteYn(board.getDeleteYn())  // deleteYn 필드 추가
                .distributorNo(board.getMember() != null && board.getMember().getDistributor() != null ? 
                		board.getMember().getDistributor().getDistributorNo() : null)
                .build();
    }
}
