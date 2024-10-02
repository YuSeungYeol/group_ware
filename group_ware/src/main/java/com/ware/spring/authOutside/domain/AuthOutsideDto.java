package com.ware.spring.authOutside.domain;

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
public class AuthOutsideDto {

    private Long authorNo;
    private Long memberNo;
    private String outsideTitle;
    private String outsideContent;
    private String outsideStatus;
    private LocalDateTime outsideRegDate;
    private LocalDateTime outsideModDate;

    public AuthOutside toEntity(Member member) {
        return AuthOutside.builder()
        		.authorNo(this.authorNo)
                .member(member)
                .outsideTitle(outsideTitle)
                .outsideContent(outsideContent)
                .outsideStatus(outsideStatus)
                .outsideRegDate(outsideRegDate)
                .outsideModDate(outsideModDate)
                .build();
    }

    public static AuthOutsideDto toDto(AuthOutside authOutside) {
        return AuthOutsideDto.builder()
        		.authorNo(authOutside.getAuthorNo())
                .memberNo(authOutside.getMember().getMemNo())
                .outsideTitle(authOutside.getOutsideTitle())
                .outsideContent(authOutside.getOutsideContent())
                .outsideStatus(authOutside.getOutsideStatus())
                .outsideRegDate(authOutside.getOutsideRegDate())
                .outsideModDate(authOutside.getOutsideModDate())
                .build();
    }
}
