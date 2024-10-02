package com.ware.spring.authOvertime.domain;

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
public class AuthOvertimeDto {

    private Long authorNo;
    private Long memberNo;
    private String overtimeTitle;
    private String overtimeContent;
    private String overtimeStatus;
    private LocalDateTime overtimeRegDate;
    private LocalDateTime overtimeModDate;

    public AuthOvertime toEntity(Member member) {
        return AuthOvertime.builder()
        		.authorNo(this.authorNo)
                .member(member)
                .overtimeTitle(overtimeTitle)
                .overtimeContent(overtimeContent)
                .overtimeStatus(overtimeStatus)
                .overtimeRegDate(overtimeRegDate)
                .overtimeModDate(overtimeModDate)
                .build();
    }

    public static AuthOvertimeDto toDto(AuthOvertime authOvertime) {
        return AuthOvertimeDto.builder()
        		.authorNo(authOvertime.getAuthorNo())
                .memberNo(authOvertime.getMember().getMemNo())
                .overtimeTitle(authOvertime.getOvertimeTitle())
                .overtimeContent(authOvertime.getOvertimeContent())
                .overtimeStatus(authOvertime.getOvertimeStatus())
                .overtimeRegDate(authOvertime.getOvertimeRegDate())
                .overtimeModDate(authOvertime.getOvertimeModDate())
                .build();
    }
}
