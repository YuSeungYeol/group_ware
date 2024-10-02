package com.ware.spring.document.domain;

import java.time.LocalDateTime;

import com.ware.spring.member.domain.Member;

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
public class DocumentDto {

    private Long documentNo;
    private int memberNo;
    private String memberName;
    private String documentTitle;
    private String documentContent;
    private String documentStatus;
    private Long createdBy;
    private LocalDateTime documentRegDate;
    private LocalDateTime documentModDate;
    private String documentSort;
    
    // DTO에서 Entity로 변환
    public Document toEntity(Member member, Member createdBy) {
        return Document.builder()
                .documentNo(documentNo)
                .member(member)
                .documentTitle(documentTitle)
                .documentContent(documentContent)
                .documentStatus(documentStatus)
                .createdBy(createdBy)
                .documentRegDate(documentRegDate)
                .documentModDate(documentModDate)
                .documentSort(documentSort)
                .build();
    }

    public static DocumentDto toDto(Document document) {
        return DocumentDto.builder()
                .documentNo(document.getDocumentNo())
                .memberName(document.getMember().getMemName())
                .documentTitle(document.getDocumentTitle())
                .documentContent(document.getDocumentContent())
                .documentStatus(document.getDocumentStatus())
                .createdBy(document.getCreatedBy().getMemNo())
                .documentRegDate(document.getDocumentRegDate())
                .documentModDate(document.getDocumentModDate())
                .documentSort(document.getDocumentSort())
                .build();
    }
}
