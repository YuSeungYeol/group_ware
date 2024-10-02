package com.ware.spring.authOvertime.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.authOvertime.domain.AuthOvertime;
import com.ware.spring.authOvertime.domain.AuthOvertimeDto;
import com.ware.spring.authOvertime.repository.AuthOvertimeRepository;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.repository.AuthorizationRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

@Service
public class AuthOvertimeService {

    private final AuthOvertimeRepository authOvertimeRepository;
    private final MemberRepository memberRepository;
    private final AuthorizationRepository authorizationRepository;
    
    @Autowired
    public AuthOvertimeService(AuthOvertimeRepository authOvertimeRepository, MemberRepository memberRepository, AuthorizationRepository authorizationRepository) {
        this.authOvertimeRepository = authOvertimeRepository;
        this.memberRepository = memberRepository;
        this.authorizationRepository = authorizationRepository;
    }

    public List<AuthOvertimeDto> selectAuthOvertimeList() {
        List<AuthOvertime> authOvertimeList = authOvertimeRepository.findAll();
        List<AuthOvertimeDto> authOvertimeDtoList = new ArrayList<>();
        for (AuthOvertime a : authOvertimeList) {
            AuthOvertimeDto dto = AuthOvertimeDto.toDto(a);
            authOvertimeDtoList.add(dto);
        }
        return authOvertimeDtoList;
    }
    
    public AuthOvertime createAuthOvertime(AuthOvertimeDto dto, MultipartFile file) {
        Member member = null;
        if (dto.getMemberNo() != null) {
            member = memberRepository.findById(dto.getMemberNo()).orElse(null);
        }

        AuthOvertime authOvertime = dto.toEntity(member);

        // Authorization 객체를 먼저 생성하고 저장
        Authorization authorization = Authorization.builder()
                .authorName(dto.getOvertimeTitle())
                .authorStatus("P")
                .member(member)  // member가 null일 경우에도 처리
                .build();

        Authorization savedAuthorization = authorizationRepository.save(authorization);
        
        if (savedAuthorization.getAuthorNo() == null) {
            throw new IllegalStateException("Authorization ID is null after saving.");
        }

        // AuthOvertime와 Authorization 연관 관계 설정
        authOvertime.setAuthorization(savedAuthorization);
        savedAuthorization.setAuthOvertime(authOvertime);

        // AuthOvertime를 저장
        authOvertime = authOvertimeRepository.save(authOvertime);
        authorizationRepository.save(savedAuthorization);

        return authOvertime;
    }
}
