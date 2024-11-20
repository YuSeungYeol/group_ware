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
    
    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **지각 사유서 목록 조회**: 데이터베이스에서 저장된 모든 지각 사유서를 조회하여 DTO 형태로 반환.
     *
     * ### 기술
     * - **Spring Data JPA**: `authLateRepository.findAll()`을 통해 데이터베이스에서 모든 `AuthLate` 엔티티를 조회.
     * - **DTO 변환**: `AuthLate` 엔티티를 `AuthLateDto`로 변환하여 데이터를 전달.
     * - **Java Collections**: 조회된 데이터를 `List`로 관리.
     *
     * ### 구현
     * - `authLateRepository`를 사용해 모든 `AuthLate` 엔티티를 조회.
     * - 엔티티 객체를 순회하며 `AuthLateDto.toDto()` 메서드를 호출해 DTO로 변환.
     * - 변환된 DTO 객체를 새로운 `List`에 추가하고 반환.
     */
    public List<AuthLateDto> selectAuthLateList() {
        List<AuthLate> authLateList = authLateRepository.findAll();
        List<AuthLateDto> authLateDtoList = new ArrayList<>();
        for (AuthLate a : authLateList) {
            AuthLateDto dto = AuthLateDto.toDto(a);
            authLateDtoList.add(dto);
        }
        return authLateDtoList;
    }
    
    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **지각 사유서 생성**: DTO를 기반으로 지각 사유서(`AuthLate`)와 권한 요청(`Authorization`) 객체를 생성하여 저장.
     * - **연관 관계 설정**: `AuthLate`와 `Authorization` 간의 양방향 연관 관계를 설정.
     *
     * ### 기술
     * - **Spring Data JPA**: `authLateRepository`와 `authorizationRepository`를 사용하여 데이터베이스에 객체 저장.
     * - **DTO 변환**: `AuthLateDto`를 `AuthLate` 엔티티로 변환.
     * - **MultipartFile 처리**: 파일 업로드에 대한 확장 가능성 고려.
     * - **Cascade**: 엔티티 간 연관 관계를 기반으로 자동 저장.
     *
     * ### 구현
     * - DTO를 기반으로 `AuthLate` 엔티티를 생성하고 연관 회원 정보 설정.
     * - `Authorization` 객체를 생성하여 기본 상태("P")와 제목을 설정한 뒤 저장.
     * - 저장된 `Authorization` 객체와 `AuthLate` 객체 간 연관 관계를 설정하고 저장.
     * - 최종적으로 생성된 `AuthLate` 객체를 반환.
     */
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
