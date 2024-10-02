package com.ware.spring.authLate.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.authLate.domain.AuthLate;
import com.ware.spring.authLate.domain.AuthLateDto;
import com.ware.spring.authLate.service.AuthLateService;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.service.AuthorizationFileService;
import com.ware.spring.authorization.service.AuthorizationService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

@Controller
public class AuthLateApiController {

    private final AuthLateService authLateService;
    private final AuthorizationService authorizationService;
    private final MemberRepository memberRepository;
    private final AuthorizationFileService authorizationFileService;

    @Autowired
    public AuthLateApiController(AuthLateService authLateService, AuthorizationService authorizationService, MemberRepository memberRepository, AuthorizationFileService authorizationFileService) {
        this.authLateService = authLateService;
        this.authorizationService = authorizationService;
        this.memberRepository = memberRepository;
        this.authorizationFileService = authorizationFileService;
    }

    @ResponseBody
    @PostMapping("/authLate")
    public Map<String, String> createAuthLate(AuthLateDto authLateDto, 
            @RequestParam("file") MultipartFile file) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "지각 사유서 등록 중 오류가 발생했습니다.");

        Member member = memberRepository.findById(authLateDto.getMemberNo()).orElse(null);

        if (member == null) {
            resultMap.put("res_msg", "유효하지 않은 회원 ID입니다.");
            return resultMap;
        }

        // AuthLate 객체 생성
        AuthLate authLate = authLateDto.toEntity(member);

        // Authorization 객체 생성 및 AuthLate와의 연관 관계 설정
        Authorization authorization = Authorization.builder()
                .authorName(authLateDto.getLateTitle())
                .authorStatus("P")
                // .member()
                .authLate(authLate)  
                .build();

        // Authorization 저장 (CascadeType.ALL 덕분에 AuthLate도 자동으로 저장됨)
        Authorization savedAuthorization = authorizationService.createAuthorization(authorization);
        
        if (savedAuthorization == null) {
            resultMap.put("res_msg", "Authorization 생성 중 오류가 발생했습니다.");
            return resultMap;
        }

        resultMap.put("res_code", "200");
        resultMap.put("res_msg", "지각 사유서가 성공적으로 등록되었습니다.");
        return resultMap;
    }
}