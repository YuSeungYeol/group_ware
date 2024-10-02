package com.ware.spring.authLate.domain;

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
public class AuthLateDto {

    private Long authorNo;
    private Long memberNo;
    private String lateTitle;
    private String lateContent;
    private String lateStatus;
    private LocalDateTime lateRegDate;
    private LocalDateTime lateModDate;

    public AuthLate toEntity(Member member) {
        return AuthLate.builder()
                .authorNo(this.authorNo)
                .member(member)
                .lateTitle(lateTitle)
                .lateContent(lateContent)
                .lateStatus(lateStatus)
                .lateRegDate(lateRegDate)
                .lateModDate(lateModDate)
                .build();
    }

    public static AuthLateDto toDto(AuthLate authLate) {
        return AuthLateDto.builder()
                .authorNo(authLate.getAuthorNo())
                .memberNo(authLate.getMember().getMemNo())
                .lateTitle(authLate.getLateTitle())
                .lateContent(authLate.getLateContent())
                .lateStatus(authLate.getLateStatus())
                .lateRegDate(authLate.getLateRegDate())
                .lateModDate(authLate.getLateModDate())
                .build();
    }
}
