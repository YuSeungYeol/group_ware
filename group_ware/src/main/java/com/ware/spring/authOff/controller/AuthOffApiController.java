package com.ware.spring.authOff.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.authOff.domain.AuthOff;
import com.ware.spring.authOff.domain.AuthOffDto;
import com.ware.spring.authOff.service.AuthOffService;
import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.service.AuthorizationFileService;
import com.ware.spring.authorization.service.AuthorizationService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

@Controller
public class AuthOffApiController {

    private final AuthOffService authOffService;
    private final AuthorizationService authorizationService;
    private final MemberRepository memberRepository;
    private final AuthorizationFileService authorizationFileService;

    @Autowired
    public AuthOffApiController(AuthOffService authOffService, AuthorizationService authorizationService, MemberRepository memberRepository, AuthorizationFileService authorizationFileService) {
        this.authOffService = authOffService;
        this.authorizationService = authorizationService;
        this.memberRepository = memberRepository;
        this.authorizationFileService = authorizationFileService;
    }

    @ResponseBody
    @PostMapping("/authOff")
    public Map<String, String> createAuthOff(AuthOffDto authOffDto, 
            @RequestParam("file") MultipartFile file) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "휴가 신청서 등록 중 오류가 발생했습니다.");

        Member member = memberRepository.findById(authOffDto.getMemberNo()).orElse(null);

        if (member == null) {
            resultMap.put("res_msg", "유효하지 않은 회원 ID입니다.");
            return resultMap;
        }

        // AuthOff 객체 생성
        AuthOff authOff = authOffDto.toEntity(member);

        // Authorization 객체 생성 및 AuthOff와의 연관 관계 설정
        Authorization authorization = Authorization.builder()
                .authorName(authOffDto.getOffTitle())
                .authorStatus("P")
//                .member()
                .authOff(authOff)  // 여기서 직접 설정
                .build();

        // Authorization 저장 (CascadeType.ALL 덕분에 AuthOff도 자동으로 저장됨)
        Authorization savedAuthorization = authorizationService.createAuthorization(authorization);
        
        
        if (savedAuthorization == null) {
            resultMap.put("res_msg", "Authorization 생성 중 오류가 발생했습니다.");
            return resultMap;
        }

        resultMap.put("res_code", "200");
        resultMap.put("res_msg", "휴가 신청서가 성공적으로 등록되었습니다.");
        return resultMap;
    }



}
