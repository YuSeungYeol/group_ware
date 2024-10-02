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

    public List<AuthOffDto> selectAuthOffList() {
        List<AuthOff> authOffList = authOffRepository.findAll();
        List<AuthOffDto> authOffDtoList = new ArrayList<>();
        for (AuthOff a : authOffList) {
            AuthOffDto dto = AuthOffDto.toDto(a);
            authOffDtoList.add(dto);
        }
        return authOffDtoList;
    }
    
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