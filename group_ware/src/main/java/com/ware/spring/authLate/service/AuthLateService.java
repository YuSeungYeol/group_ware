package com.ware.spring.authLate.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.authLate.domain.AuthLate;
import com.ware.spring.authLate.domain.AuthLateDto;
import com.ware.spring.authLate.repository.AuthLateRepository;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.repository.AuthorizationRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

@Service
public class AuthLateService {

    private final AuthLateRepository authLateRepository;
    private final MemberRepository memberRepository;
    private final AuthorizationRepository authorizationRepository;
    
    @Autowired
    public AuthLateService(AuthLateRepository authLateRepository, MemberRepository memberRepository, AuthorizationRepository authorizationRepository) {
        this.authLateRepository = authLateRepository;
        this.memberRepository = memberRepository;
        this.authorizationRepository = authorizationRepository;
    }

    public List<AuthLateDto> selectAuthLateList() {
        List<AuthLate> authLateList = authLateRepository.findAll();
        List<AuthLateDto> authLateDtoList = new ArrayList<>();
        for (AuthLate a : authLateList) {
            AuthLateDto dto = AuthLateDto.toDto(a);
            authLateDtoList.add(dto);
        }
        return authLateDtoList;
    }
    
    public AuthLate createAuthLate(AuthLateDto dto, MultipartFile file) {
        Member member = null;
        if (dto.getMemberNo() != null) {
            member = memberRepository.findById(dto.getMemberNo()).orElse(null);
        }

        AuthLate authLate = dto.toEntity(member);

        // Authorization 객체를 먼저 생성하고 저장
        Authorization authorization = Authorization.builder()
                .authorName(dto.getLateTitle())
                .authorStatus("P")
                .member(member)  // member가 null일 경우에도 처리
                .build();

        Authorization savedAuthorization = authorizationRepository.save(authorization);
        
        System.out.println("Author Name: " + savedAuthorization.getAuthorName());
        System.out.println("Author Status: " + savedAuthorization.getAuthorStatus());
        System.out.println("Member No: " + savedAuthorization.getMember().getMemNo());
        
        if (savedAuthorization.getAuthorNo() == null) {
            throw new IllegalStateException("Authorization ID is null after saving.");
        }

        // AuthLate와 Authorization 연관 관계 설정
        authLate.setAuthorization(savedAuthorization);
        savedAuthorization.setAuthLate(authLate);

        // AuthLate를 저장
        authLate = authLateRepository.save(authLate);
        authorizationRepository.save(savedAuthorization);

        return authLate;
    }
}
