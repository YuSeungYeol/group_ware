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

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **지각 사유서 등록**: 사용자가 제출한 지각 사유서를 저장하며, 회원 정보 및 첨부 파일을 처리.
     * - **연관 엔티티 생성**: `AuthLate`와 `Authorization` 객체를 생성하고 관계 설정.
     * - **상태 기반 응답**: 요청 성공/실패에 따라 적절한 메시지를 반환.
     *
     * ### 기술
     * - **Spring MVC**: `@PostMapping`과 `@ResponseBody`를 활용하여 RESTful API로 구현.
     * - **JPA**: `CascadeType.ALL` 설정을 통해 연관 엔티티(`AuthLate`, `Authorization`) 자동 저장.
     * - **MultipartFile 처리**: 첨부 파일을 매핑하여 업로드 가능.
     * - **CSRF 보호**: Spring Security CSRF 토큰을 활용한 보안 강화.
     * - **JSON 응답**: 상태 코드와 메시지를 `Map` 형태로 반환하여 클라이언트와 통신.
     *
     * ### 구현
     * 1. **응답 기본값 설정**:
     *    - 기본 응답 코드를 `404`로 설정하고, 오류 메시지 초기화.
     * 
     * 2. **회원 검증**:
     *    - `authLateDto`에서 전달된 `memberNo`를 사용해 회원 정보를 조회.
     *    - 유효하지 않은 `memberNo`인 경우 오류 메시지를 반환.
     *
     * 3. **지각 사유서 엔티티 생성**:
     *    - DTO 데이터를 기반으로 `AuthLate` 객체를 생성.
     * 
     * 4. **승인 엔티티 생성**:
     *    - `Authorization` 객체를 생성하고 지각 사유서(`AuthLate`)와 연관 관계 설정.
     *    - 기본 승인 상태를 **"P" (Pending)**로 설정.
     * 
     * 5. **엔티티 저장**:
     *    - `Authorization` 객체를 저장하며, `CascadeType.ALL`로 `AuthLate`도 자동 저장.
     *    - 저장 결과에 따라 성공 여부를 결정.
     * 
     * 6. **응답 반환**:
     *    - 성공 시 `200` 응답 코드와 성공 메시지를 반환.
     *    - 실패 시 초기 설정된 오류 메시지를 유지하여 반환.
     */
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