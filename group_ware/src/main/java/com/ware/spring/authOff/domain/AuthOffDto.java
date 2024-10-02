package com.ware.spring.authOff.domain;

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
public class AuthOffDto {

    private Long authorNo;
    private Long memberNo;
    private String offTitle;
    private String offContent;
    private String offStatus;
    private LocalDateTime offRegDate;
    private LocalDateTime offModDate;

    public AuthOff toEntity(Member member) {
        return AuthOff.builder()
                .authorNo(this.authorNo)
                .member(member)
                .offTitle(offTitle)
                .offContent(offContent)
                .offStatus(offStatus)
                .offRegDate(offRegDate)
                .offModDate(offModDate)
                .build();
    }

    public static AuthOffDto toDto(AuthOff authOff) {
        return AuthOffDto.builder()
                .authorNo(authOff.getAuthorNo())
                .memberNo(authOff.getMember().getMemNo())
                .offTitle(authOff.getOffTitle())
                .offContent(authOff.getOffContent())
                .offStatus(authOff.getOffStatus())
                .offRegDate(authOff.getOffRegDate())
                .offModDate(authOff.getOffModDate())
                .build();
    }

}
