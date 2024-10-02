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

    public List<AuthTripDto> selectAuthTripList() {
        List<AuthTrip> authTripList = authTripRepository.findAll();
        List<AuthTripDto> authTripDtoList = new ArrayList<>();
        for (AuthTrip a : authTripList) {
            AuthTripDto dto = AuthTripDto.toDto(a);
            authTripDtoList.add(dto);
        }
        return authTripDtoList;
    }
    
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
