package com.ware.spring.member.domain;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private Long mem_no;
    private String mem_id;
    private String mem_pw;
    private String mem_name;
    private String mem_email;
    private String mem_phone;
    private Long rank_no;
    private Long distributor_no;
    private String rank_name;
    private LocalDate mem_reg_date;
    private LocalDate mem_mod_date; 
    private String profile_saved;
    private String emp_no;  // 사원번호 추가
    private double mem_off;
    private double mem_use_off;
    private List<GrantedAuthority> authorities;
    private String distributor_name;  // 추가
    private String mem_leave;
    public Member toEntity(Rank rank, Distributor distributor) {
        return Member.builder()
                .memNo(mem_no)
                .memId(mem_id)
                .memPw(mem_pw)
                .memName(mem_name)
                .memPhone(mem_phone)
                .memEmail(mem_email)
                .profileSaved(profile_saved)
                .rank(rank)  // rank_no 사용
                .distributor(distributor)
                .memRegDate(mem_reg_date)
                .memModDate(mem_mod_date) 
                .memOff(mem_off)
                .memUseOff(mem_use_off)
                .empNo(emp_no)
                .memLeave(mem_leave)
                .build();
    }
    public static MemberDto toDto(Member member) {
        return MemberDto.builder()
            .mem_no(member.getMemNo())
            .mem_id(member.getMemId())
            .mem_pw(member.getMemPw())  // 비밀번호도 포함할 경우
            .mem_name(member.getMemName())
            .mem_phone(member.getMemPhone())
            .mem_email(member.getMemEmail())
            .rank_no(member.getRank().getRankNo())  // 직급 번호
            .rank_name(member.getRank().getRankName())  // 직급 이름 추가
            .distributor_no(member.getDistributor().getDistributorNo())  // 지점 번호
            .mem_reg_date(member.getMemRegDate())
            .mem_mod_date(member.getMemModDate()) 
            .profile_saved(member.getProfileSaved())
            .mem_leave(member.getMemLeave())
            .emp_no(member.getEmpNo())  // 사원번호
            .distributor_name(member.getDistributor().getDistributorName())  // distributor_name 설정
            .mem_off(member.getMemOff())
            .mem_use_off(member.getMemUseOff())
            .build();
    }


}
