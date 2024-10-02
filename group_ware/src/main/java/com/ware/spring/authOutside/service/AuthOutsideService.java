package com.ware.spring.authOutside.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.authOutside.domain.AuthOutside;
import com.ware.spring.authOutside.domain.AuthOutsideDto;
import com.ware.spring.authOutside.repository.AuthOutsideRepository;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.repository.AuthorizationRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

@Service
public class AuthOutsideService {

    private final AuthOutsideRepository authOutsideRepository;
    private final MemberRepository memberRepository;
    private final AuthorizationRepository authorizationRepository;
    
    @Autowired
    public AuthOutsideService(AuthOutsideRepository authOutsideRepository, MemberRepository memberRepository, AuthorizationRepository authorizationRepository) {
        this.authOutsideRepository = authOutsideRepository;
        this.memberRepository = memberRepository;
        this.authorizationRepository = authorizationRepository;
    }

    public List<AuthOutsideDto> selectAuthOutsideList() {
        List<AuthOutside> authOutsideList = authOutsideRepository.findAll();
        List<AuthOutsideDto> authOutsideDtoList = new ArrayList<>();
        for (AuthOutside a : authOutsideList) {
            AuthOutsideDto dto = AuthOutsideDto.toDto(a);
            authOutsideDtoList.add(dto);
        }
        return authOutsideDtoList;
    }
    
    public AuthOutside createAuthOutside(AuthOutsideDto dto, MultipartFile file) {
        Member member = null;
        if (dto.getMemberNo() != null) {
            member = memberRepository.findById(dto.getMemberNo()).orElse(null);
        }

        AuthOutside authOutside = dto.toEntity(member);

        // Authorization 객체를 먼저 생성하고 저장
        Authorization authorization = Authorization.builder()
                .authorName(dto.getOutsideTitle())
                .authorStatus("P")
                .member(member)  // member가 null일 경우에도 처리
                .build();

        Authorization savedAuthorization = authorizationRepository.save(authorization);
        
        if (savedAuthorization.getAuthorNo() == null) {
            throw new IllegalStateException("Authorization ID is null after saving.");
        }

        // AuthOutside와 Authorization 연관 관계 설정
        authOutside.setAuthorization(savedAuthorization);
        savedAuthorization.setAuthOutside(authOutside);

        // AuthOutside를 저장
        authOutside = authOutsideRepository.save(authOutside);
        authorizationRepository.save(savedAuthorization);

        return authOutside;
    }


	
}

