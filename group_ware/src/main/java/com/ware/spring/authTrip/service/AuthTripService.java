package com.ware.spring.authTrip.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.authTrip.domain.AuthTrip;
import com.ware.spring.authTrip.domain.AuthTripDto;
import com.ware.spring.authTrip.repository.AuthTripRepository;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.repository.AuthorizationRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

@Service
public class AuthTripService {

    private final AuthTripRepository authTripRepository;
    private final MemberRepository memberRepository;
    private final AuthorizationRepository authorizationRepository;
    
    @Autowired
    public AuthTripService(AuthTripRepository authTripRepository, MemberRepository memberRepository, AuthorizationRepository authorizationRepository) {
        this.authTripRepository = authTripRepository;
        this.memberRepository = memberRepository;
        this.authorizationRepository = authorizationRepository;
    }
    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **출장 신청 목록 조회**: 모든 해외 출장 신청서를 데이터베이스에서 조회하여 DTO 리스트로 반환합니다.
     * - **데이터 변환**: `AuthTrip` 엔터티 객체를 `AuthTripDto`로 변환하여 프레젠테이션 레이어로 전달합니다.
     *
     * ### 기술
     * - **Spring Data JPA**: `authTripRepository.findAll()`을 통해 데이터베이스에서 모든 `AuthTrip` 엔터티를 조회합니다.
     * - **DTO 변환**: 엔터티를 DTO로 변환하여 데이터 전송 및 표현 계층에서 사용됩니다.
     * - **Java Collection Framework**: `ArrayList`를 사용하여 DTO 리스트를 생성합니다.
     *
     * ### 구현
     * 1. **엔터티 조회**:
     *    - `authTripRepository.findAll()`을 호출하여 데이터베이스에 저장된 모든 `AuthTrip` 엔터티를 가져옵니다.
     * 2. **DTO 변환**:
     *    - 각 `AuthTrip` 엔터티를 순회하며 `AuthTripDto.toDto(a)`를 호출하여 DTO 객체로 변환합니다.
     * 3. **결과 반환**:
     *    - 변환된 DTO 객체들을 리스트에 추가한 후 최종적으로 DTO 리스트를 반환합니다.
     */
    public List<AuthTripDto> selectAuthTripList() {
        List<AuthTrip> authTripList = authTripRepository.findAll();
        List<AuthTripDto> authTripDtoList = new ArrayList<>();
        for (AuthTrip a : authTripList) {
            AuthTripDto dto = AuthTripDto.toDto(a);
            authTripDtoList.add(dto);
        }
        return authTripDtoList;
    }
    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **해외 출장 신청 생성**: DTO 데이터를 기반으로 `AuthTrip` 엔터티를 생성 및 저장합니다.
     * - **결재 승인 정보 연동**: 생성된 출장 신청과 결재 승인 정보를 연동하여 데이터의 일관성을 유지합니다.
     * - **예외 처리**: 필수 데이터 누락 및 저장 오류를 방지하기 위해 예외를 처리합니다.
     *
     * ### 기술
     * - **Spring Data JPA**: `authTripRepository`와 `authorizationRepository`를 사용하여 데이터베이스에 엔터티를 저장 및 연동합니다.
     * - **DTO 변환**: `AuthTripDto.toEntity(member)`를 호출하여 DTO 데이터를 엔터티로 변환합니다.
     * - **Builder 패턴**: `Authorization.builder()`를 통해 객체 생성 시 가독성과 유지 보수를 용이하게 합니다.
     * - **엔터티 연관 관계 매핑**: `authTrip.setAuthorization(savedAuthorization)`를 통해 `AuthTrip`과 `Authorization` 간의 관계를 설정합니다.
     *
     * ### 구현
     * 1. **회원 정보 조회**:
     *    - DTO에 포함된 `memberNo`를 기반으로 `memberRepository.findById()`를 호출하여 회원 정보를 조회합니다.
     *    - 회원 정보가 없으면 `member`를 `null`로 설정하여 처리합니다.
     * 2. **출장 신청 엔터티 생성**:
     *    - `dto.toEntity(member)`를 호출하여 `AuthTrip` 엔터티를 생성합니다.
     * 3. **결재 승인 정보 생성**:
     *    - `Authorization.builder()`를 호출하여 결재 승인 정보를 생성합니다.
     *    - 결재 상태를 기본값 "P(대기)"로 설정합니다.
     * 4. **엔터티 저장 및 연관 설정**:
     *    - `authorizationRepository.save()`를 통해 결재 승인 정보를 저장합니다.
     *    - 저장된 결재 승인 정보를 `AuthTrip` 엔터티와 연동한 후, 두 엔터티를 각각 저장합니다.
     * 5. **결과 반환**:
     *    - 최종적으로 저장된 `AuthTrip` 엔터티를 반환합니다.
     */
    public AuthTrip createAuthTrip(AuthTripDto dto, MultipartFile file) {
        Member member = null;
        if (dto.getMemberNo() != null) {
            member = memberRepository.findById(dto.getMemberNo()).orElse(null);
        }

        AuthTrip authTrip = dto.toEntity(member);

        Authorization authorization = Authorization.builder()
                .authorName(dto.getTripTitle())
                .authorStatus("P")
                .member(member)
                .build();

        Authorization savedAuthorization = authorizationRepository.save(authorization);
        
        System.out.println("Author Name: " + savedAuthorization.getAuthorName());
        System.out.println("Author Status: " + savedAuthorization.getAuthorStatus());
        System.out.println("Member No: " + savedAuthorization.getMember().getMemNo());
        
        if (savedAuthorization.getAuthorNo() == null) {
            throw new IllegalStateException("Authorization ID is null after saving.");
        }

        authTrip.setAuthorization(savedAuthorization);
        savedAuthorization.setAuthTrip(authTrip);

        authTrip = authTripRepository.save(authTrip);
        authorizationRepository.save(savedAuthorization);

        return authTrip;
    }
}
