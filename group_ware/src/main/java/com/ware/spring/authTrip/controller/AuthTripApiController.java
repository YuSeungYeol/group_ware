package com.ware.spring.authTrip.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.authTrip.domain.AuthTrip;
import com.ware.spring.authTrip.domain.AuthTripDto;
import com.ware.spring.authTrip.service.AuthTripService;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.service.AuthorizationFileService;
import com.ware.spring.authorization.service.AuthorizationService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

@Controller
public class AuthTripApiController {

    private final AuthTripService authTripService;
    private final AuthorizationService authorizationService;
    private final MemberRepository memberRepository;
    private final AuthorizationFileService authorizationFileService;

    @Autowired
    public AuthTripApiController(AuthTripService authTripService, AuthorizationService authorizationService, MemberRepository memberRepository, AuthorizationFileService authorizationFileService) {
        this.authTripService = authTripService;
        this.authorizationService = authorizationService;
        this.memberRepository = memberRepository;
        this.authorizationFileService = authorizationFileService;
    }

    @ResponseBody
    @PostMapping("/authTrip")
    public Map<String, String> createAuthTrip(AuthTripDto authTripDto, 
            @RequestParam("file") MultipartFile file) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "해외 출장 신청서 등록 중 오류가 발생했습니다.");

        Member member = memberRepository.findById(authTripDto.getMemberNo()).orElse(null);

        if (member == null) {
            resultMap.put("res_msg", "유효하지 않은 회원 ID입니다.");
            return resultMap;
        }

        // AuthTrip 객체 생성
        AuthTrip authTrip = authTripDto.toEntity(member);

        // Authorization 객체 생성 및 AuthTrip와의 연관 관계 설정
        Authorization authorization = Authorization.builder()
                .authorName(authTripDto.getTripTitle())
                .authorStatus("P")
                .authTrip(authTrip)  // 여기서 직접 설정
                .build();

        // Authorization 저장 (CascadeType.ALL 덕분에 AuthTrip도 자동으로 저장됨)
        Authorization savedAuthorization = authorizationService.createAuthorization(authorization);
        
        if (savedAuthorization == null) {
            resultMap.put("res_msg", "Authorization 생성 중 오류가 발생했습니다.");
            return resultMap;
        }

        resultMap.put("res_code", "200");
        resultMap.put("res_msg", "해외 출장 신청서가 성공적으로 등록되었습니다.");
        return resultMap;
    }
}