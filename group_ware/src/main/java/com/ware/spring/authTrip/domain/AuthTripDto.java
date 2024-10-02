package com.ware.spring.authTrip.domain;

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
public class AuthTripDto {

    private Long authorNo;
    private Long memberNo;
    private String tripTitle;
    private String tripContent;
    private String tripStatus;
    private LocalDateTime tripRegDate;
    private LocalDateTime tripModDate;

    public AuthTrip toEntity(Member member) {
        return AuthTrip.builder()
                .authorNo(this.authorNo)
                .member(member)
                .tripTitle(tripTitle)
                .tripContent(tripContent)
                .tripStatus(tripStatus)
                .tripRegDate(tripRegDate)
                .tripModDate(tripModDate)
                .build();
    }

    public static AuthTripDto toDto(AuthTrip authTrip) {
        return AuthTripDto.builder()
                .authorNo(authTrip.getAuthorNo())
                .memberNo(authTrip.getMember().getMemNo())
                .tripTitle(authTrip.getTripTitle())
                .tripContent(authTrip.getTripContent())
                .tripStatus(authTrip.getTripStatus())
                .tripRegDate(authTrip.getTripRegDate())
                .tripModDate(authTrip.getTripModDate())
                .build();
    }
}
