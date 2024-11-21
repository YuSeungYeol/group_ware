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

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **해외 출장 신청서 등록**: 사용자가 제출한 데이터를 바탕으로 해외 출장 신청서를 생성하고 저장합니다.
     * - **연관 관계 설정**: 생성된 출장 신청서를 Authorization 객체와 연관 지어 저장합니다.
     * - **결과 반환**: 신청 결과를 사용자에게 JSON 형태로 반환하여 성공 여부를 알려줍니다.
     *
     * ### 기술
     * - **Spring Data JPA**: `Member`, `Authorization`, `AuthTrip` 엔티티 간의 연관 관계를 설정하고 데이터베이스와 상호작용합니다.
     * - **MultipartFile**: 첨부 파일을 처리하기 위해 사용됩니다.
     * - **REST API**: `@PostMapping` 및 `@ResponseBody`를 사용하여 JSON 데이터를 반환합니다.
     * - **DTO 패턴**: `AuthTripDto`를 통해 요청 데이터를 안전하게 전달하고 처리합니다.
     * - **Service Layer**: 비즈니스 로직을 서비스 계층(`authorizationService`)에서 처리하여 책임 분리를 구현합니다.
     *
     * ### 구현
     * 1. **Member 유효성 검사**:
     *    - `authTripDto`에서 전달받은 `memberNo`로 `Member`를 조회하며, 유효하지 않은 경우 오류 메시지를 반환합니다.
     * 2. **AuthTrip 생성**:
     *    - DTO 객체의 데이터를 바탕으로 `AuthTrip` 엔티티를 생성합니다.
     * 3. **Authorization 생성 및 저장**:
     *    - Authorization 객체를 생성하여 상태값(`P`) 및 제목(`tripTitle`)을 설정하고 `authTrip`과 연관을 맺습니다.
     *    - `authorizationService.createAuthorization()`을 통해 저장하며, CascadeType.ALL 설정으로 `authTrip`도 자동 저장됩니다.
     * 4. **결과 반환**:
     *    - 저장 성공 시 성공 코드(`200`)와 메시지를 반환하고, 실패 시 오류 메시지를 반환합니다.
     */
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