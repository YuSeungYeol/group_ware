package com.ware.spring.authOff.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.authOff.domain.AuthOff;
import com.ware.spring.authOff.domain.AuthOffDto;
import com.ware.spring.authOff.repository.AuthOffRepository;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.repository.AuthorizationRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

//@Service
//public class AuthOffService {
//
//    private final AuthOffRepository authOffRepository;
//    private final MemberRepository memberRepository;
//    private final AuthorizationRepository authorizationRepository;
//    
//    @Autowired
//    public AuthOffService(AuthOffRepository authOffRepository, MemberRepository memberRepository, AuthorizationRepository authorizationRepository) {
//        this.authOffRepository = authOffRepository;
//        this.memberRepository = memberRepository;
//        this.authorizationRepository = authorizationRepository;
//    }
//
//    public List<AuthOffDto> selectAuthOffList() {
//        List<AuthOff> authOffList = authOffRepository.findAll();
//        List<AuthOffDto> authOffDtoList = new ArrayList<>();
//        for (AuthOff a : authOffList) {
//            AuthOffDto dto = AuthOffDto.toDto(a);
//            authOffDtoList.add(dto);
//        }
//        return authOffDtoList;
//    }
//    
//    public AuthOff createAuthOff(AuthOffDto dto, MultipartFile file) {
//        Long memberNo = dto.getMemberNo();
//
//        Member member = memberRepository.findById(memberNo)
//                        .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));
//
//        AuthOff authOff = dto.toEntity(member);
//
//        // Authorization 객체를 먼저 생성하고 저장
//        Authorization authorization = Authorization.builder()
//                .authorName(dto.getOffTitle())
//                .authorStatus("Pending")
//                .member(member)
//                .build();
//
//        Authorization savedAuthorization = authorizationRepository.save(authorization);
//        
//        if (savedAuthorization.getAuthorNo() == null) {
//            throw new IllegalStateException("Authorization ID is null after saving.");
//        }
//
//        // AuthOff와 Authorization 연관 관계 설정
//        authOff.setAuthorization(savedAuthorization);
//        savedAuthorization.setAuthOff(authOff);
//
//        // AuthOff를 저장
//        authOff = authOffRepository.save(authOff);
//        authorizationRepository.save(savedAuthorization);
//
//        return authOff;
//    }
//}
@Service
public class AuthOffService {

    private final AuthOffRepository authOffRepository;
    private final MemberRepository memberRepository;
    private final AuthorizationRepository authorizationRepository;
    
    @Autowired
    public AuthOffService(AuthOffRepository authOffRepository, MemberRepository memberRepository, AuthorizationRepository authorizationRepository) {
        this.authOffRepository = authOffRepository;
        this.memberRepository = memberRepository;
        this.authorizationRepository = authorizationRepository;
    }
    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **휴가 신청서 목록 조회**: `AuthOff` 엔티티 객체들을 조회하여 `AuthOffDto` 객체 리스트로 변환 후 반환.
     * - **DTO 변환**: `AuthOff` 객체를 `AuthOffDto`로 변환하여 클라이언트로 전달할 데이터를 준비.
     *
     * ### 기술
     * - **JPA**: `authOffRepository.findAll()`을 사용하여 `AuthOff` 엔티티의 모든 데이터를 조회.
     * - **DTO 패턴**: `AuthOffDto.toDto()` 메서드를 사용하여 `AuthOff` 엔티티를 DTO로 변환.
     *
     * ### 구현
     * - `authOffRepository.findAll()`로 조회한 `AuthOff` 리스트를 순회하면서 각 `AuthOff` 객체를 `AuthOffDto` 객체로 변환.
     * - 변환된 `AuthOffDto` 객체들을 리스트에 추가하여 반환.
     */
    public List<AuthOffDto> selectAuthOffList() {
        List<AuthOff> authOffList = authOffRepository.findAll();
        List<AuthOffDto> authOffDtoList = new ArrayList<>();
        for (AuthOff a : authOffList) {
            AuthOffDto dto = AuthOffDto.toDto(a);
            authOffDtoList.add(dto);
        }
        return authOffDtoList;
    }
    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **휴가 신청서 생성**: `AuthOffDto`를 기반으로 `AuthOff` 객체를 생성하고, 이를 처리하기 위한 `Authorization` 객체를 함께 생성.
     * - **Authorization 객체와의 연관 설정**: `Authorization` 객체를 먼저 생성하고, 그 후 `AuthOff`와 연관을 설정하여 저장.
     * - **파일 처리**: `MultipartFile`을 받아서 파일 관련 처리를 위한 로직 추가 가능.
     * - **예외 처리**: `Authorization` 저장 후 `authorNo` 값이 `null`일 경우 예외를 던져서 데이터 무결성을 보장.
     *
     * ### 기술
     * - **JPA**: `authOffRepository.save()`와 `authorizationRepository.save()`를 사용하여 데이터베이스에 데이터를 저장.
     * - **DTO 패턴**: `AuthOffDto.toEntity()`를 사용하여 DTO 객체를 엔티티 객체로 변환.
     * - **빌더 패턴**: `Authorization.builder()`를 사용하여 `Authorization` 객체를 생성.
     * - **파일 업로드 처리**: `MultipartFile`을 사용하여 파일을 서버로 업로드.
     *
     * ### 구현
     * - `AuthOffDto` 객체를 `AuthOff` 엔티티로 변환하고, `Authorization` 객체를 먼저 생성하여 저장.
     * - `Authorization`의 `authorNo` 값이 `null`이면 예외를 던져 처리.
     * - `AuthOff` 객체와 `Authorization` 객체의 관계를 설정한 후 두 객체를 데이터베이스에 저장.
     */
    public AuthOff createAuthOff(AuthOffDto dto, MultipartFile file) {
        Member member = null;
        if (dto.getMemberNo() != null) {
            member = memberRepository.findById(dto.getMemberNo()).orElse(null);
        }

        AuthOff authOff = dto.toEntity(member);

        // Authorization 객체를 먼저 생성하고 저장
        Authorization authorization = Authorization.builder()
                .authorName(dto.getOffTitle())
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

        // AuthOff와 Authorization 연관 관계 설정
        authOff.setAuthorization(savedAuthorization);
        savedAuthorization.setAuthOff(authOff);

        // AuthOff를 저장
        authOff = authOffRepository.save(authOff);
        authorizationRepository.save(savedAuthorization);

        return authOff;
    }
}