package com.ware.spring.authLate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ware.spring.authLate.domain.AuthLateDto;
import com.ware.spring.authLate.service.AuthLateService;

@Controller
public class AuthLateViewController {

    private final AuthLateService authLateService;

    @Autowired
    public AuthLateViewController(AuthLateService authLateService) {
        this.authLateService = authLateService;
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **지각 사유서 목록 페이지 이동**: 등록된 지각 사유서를 조회하여 목록 페이지로 이동.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`으로 클라이언트 요청을 처리.
     * - **Model 활용**: `Model` 객체에 데이터 추가 후 HTML 템플릿으로 전달.
     * - **Thymeleaf**: 서버 데이터를 렌더링하여 목록 페이지 생성.
     *
     * ### 구현
     * - `AuthLateService`를 통해 모든 지각 사유서를 조회.
     * - 조회된 데이터를 `Model` 객체에 추가.
     * - 템플릿 경로를 반환하여 목록 페이지 렌더링.
     */
    @GetMapping("/authLateList")
    public String selectAuthLateList(Model model) {
        List<AuthLateDto> resultList = authLateService.selectAuthLateList();
        model.addAttribute("resultList", resultList);
        return "authorization/authorizationLateList"; // 템플릿 경로에 맞게 수정
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **지각 사유서 상세 페이지 이동**: 특정 지각 사유서의 상세 내용을 확인하는 페이지로 이동.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`으로 클라이언트 요청을 처리.
     * - **Thymeleaf**: HTML 템플릿 경로를 반환하여 상세 페이지 렌더링.
     *
     * ### 구현
     * - 요청에 대해 상세 페이지의 템플릿 경로를 반환.
     * - 서버 데이터를 전달하지 않고 단순히 페이지로 이동.
     */
    @GetMapping("/authLate")
    public String selectAuthLate() {
        return "authLate/authLateView"; 
    }
    
    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **지각 사유서 작성 페이지 이동**: 사용자가 새 지각 사유서를 작성할 수 있는 페이지로 이동.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`을 통해 GET 요청을 처리.
     * - **Thymeleaf**: 템플릿 경로를 반환하여 작성 페이지를 렌더링.
     *
     * ### 구현
     * - 요청에 대해 지각 사유서 작성 페이지의 템플릿 경로를 반환.
     * - 서버 데이터 없이 정적 페이지로 이동.
     */
    @GetMapping("/authLate/authLateCreate")
    public String createAuthLatePage() {
        return "authorization/authorizationLate"; // 템플릿 경로에 맞게 수정
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **지각 사유서 모달 페이지 이동**: 지각 사유서를 확인하거나 작성하기 위한 모달 창 페이지로 이동.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`을 사용하여 GET 요청을 처리.
     * - **Thymeleaf**: 템플릿 엔진을 사용하여 모달 페이지를 렌더링.
     *
     * ### 구현
     * - 클라이언트 요청에 대해 지각 사유서 모달 템플릿 경로를 반환.
     * - 동적 데이터 로드 없이 정적 템플릿 페이지를 표시.
     */
    @GetMapping("/authLate/authLatemodal")
    public String showAuthLateModal() {
        return "authLate/authLateModal"; // 템플릿 경로에 맞게 수정
    }
}