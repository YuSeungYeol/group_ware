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
    
    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **휴가 신청서 생성**: DTO를 기반으로 휴가 신청서(`AuthOff`)와 권한 요청(`Authorization`) 객체를 생성하여 저장.
     * - **연관 관계 설정**: `AuthOff`와 `Authorization` 간의 양방향 연관 관계를 설정.
     *
     * ### 기술
     * - **Spring Data JPA**: `authOffRepository`와 `authorizationRepository`를 사용하여 데이터베이스에 객체 저장.
     * - **DTO 변환**: `AuthOffDto`를 `AuthOff` 엔티티로 변환.
     * - **MultipartFile 처리**: 파일 업로드를 처리하는 기능 제공.
     * - **Cascade**: 엔티티 간 연관 관계를 기반으로 자동 저장.
     * - **JSON 응답**: 결과 코드와 메시지를 JSON 형태로 반환하여 프론트엔드와 통신.
     *
     * ### 구현
     * - DTO를 기반으로 `AuthOff` 엔티티를 생성하고 연관 회원 정보 설정.
     * - `Authorization` 객체를 생성하여 기본 상태("P")와 제목을 설정한 뒤 저장.
     * - 저장된 `Authorization` 객체와 `AuthOff` 객체 간 연관 관계를 설정하고 저장.
     * - 최종적으로 생성된 휴가 신청서의 성공 여부에 따라 응답을 반환.
     */
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
