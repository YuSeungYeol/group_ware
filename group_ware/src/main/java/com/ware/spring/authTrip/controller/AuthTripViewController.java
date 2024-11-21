package com.ware.spring.authTrip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ware.spring.authTrip.domain.AuthTripDto;
import com.ware.spring.authTrip.service.AuthTripService;

@Controller
public class AuthTripViewController {

    private final AuthTripService authTripService;

    @Autowired
    public AuthTripViewController(AuthTripService authTripService) {
        this.authTripService = authTripService;
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **해외 출장 신청서 목록 조회**: 모든 해외 출장 신청서를 조회하여 화면에 렌더링합니다.
     * - **데이터 전달**: 조회한 신청서를 `Model` 객체에 추가하여 뷰에서 접근 가능하도록 설정합니다.
     * - **목록 페이지 이동**: 조회 결과를 전달하며 지정된 템플릿 페이지로 이동합니다.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`을 통해 클라이언트 요청을 처리하고 뷰를 반환합니다.
     * - **Model 객체**: 컨트롤러에서 조회한 데이터를 템플릿 엔진에 전달합니다.
     * - **Thymeleaf**: `authorizationTripList` 템플릿을 사용하여 데이터를 동적으로 렌더링합니다.
     * - **Service Layer**: `authTripService`를 통해 비즈니스 로직을 처리합니다.
     * - **DTO 패턴**: 엔티티와 분리된 `AuthTripDto`를 사용해 데이터 전송의 안정성을 확보합니다.
     *
     * ### 구현
     * 1. **해외 출장 신청서 조회**:
     *    - `authTripService.selectAuthTripList()`를 호출하여 모든 신청서를 조회합니다.
     *    - 조회 결과는 `AuthTripDto` 리스트로 반환됩니다.
     * 2. **데이터 추가**:
     *    - `Model` 객체에 `resultList`라는 키로 조회 데이터를 추가하여 뷰에 전달합니다.
     * 3. **뷰 반환**:
     *    - 템플릿 경로 `authorization/authorizationTripList`로 이동하여 결과를 렌더링합니다.
     */
    @GetMapping("/authTripList")
    public String selectAuthTripList(Model model) {
        List<AuthTripDto> resultList = authTripService.selectAuthTripList();
        model.addAttribute("resultList", resultList);
        return "authorization/authorizationTripList"; // 템플릿 경로에 맞게 수정
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **해외 출장 신청서 상세 페이지 이동**: 특정 해외 출장 신청서의 세부 정보를 확인할 수 있는 페이지로 이동합니다.
     * - **정적 템플릿 렌더링**: 데이터와 관계없이 지정된 상세보기 템플릿 페이지를 반환합니다.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`을 통해 클라이언트 요청을 처리하고 뷰 경로를 반환합니다.
     * - **Thymeleaf**: 지정된 템플릿(`authTrip/authTripView`)을 렌더링합니다.
     *
     * ### 구현
     * 1. **클라이언트 요청 처리**:
     *    - 클라이언트가 `/authTrip` 경로로 GET 요청을 보낼 경우, 해당 메서드가 실행됩니다.
     * 2. **뷰 경로 반환**:
     *    - `authTrip/authTripView`라는 이름의 템플릿 경로를 반환하여 뷰를 렌더링합니다.
     */
    @GetMapping("/authTrip")
    public String selectAuthTrip() {
        return "authTrip/authTripView"; // 템플릿 경로에 맞게 수정
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **해외 출장 신청서 작성 페이지 이동**: 사용자가 해외 출장 신청서를 작성할 수 있는 페이지로 이동합니다.
     * - **정적 템플릿 렌더링**: 사용자 입력 양식을 포함한 템플릿 페이지를 반환합니다.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`을 사용하여 클라이언트의 GET 요청을 처리합니다.
     * - **Thymeleaf**: 지정된 템플릿(`authorization/authorizationTrip`)을 렌더링합니다.
     *
     * ### 구현
     * 1. **클라이언트 요청 처리**:
     *    - 클라이언트가 `/authTrip/authTripCreate` 경로로 GET 요청을 보낼 경우, 해당 메서드가 실행됩니다.
     * 2. **뷰 경로 반환**:
     *    - `authorization/authorizationTrip`라는 이름의 템플릿 경로를 반환하여 작성 페이지를 렌더링합니다.
     */
    @GetMapping("/authTrip/authTripCreate")
    public String createAuthTripPage() {
        return "authorization/authorizationTrip"; // 템플릿 경로에 맞게 수정
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **해외 출장 신청 모달 호출**: 사용자가 해외 출장 관련 정보를 확인하거나 작성할 수 있는 모달 페이지를 제공합니다.
     * - **정적 템플릿 렌더링**: 모달 형식의 사용자 인터페이스를 반환합니다.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`을 사용하여 클라이언트의 GET 요청을 처리합니다.
     * - **Thymeleaf**: 지정된 템플릿(`authTrip/authTripModal`)을 렌더링하여 모달 콘텐츠를 제공합니다.
     *
     * ### 구현
     * 1. **클라이언트 요청 처리**:
     *    - 클라이언트가 `/authTrip/authTripmodal` 경로로 GET 요청을 보낼 경우, 해당 메서드가 실행됩니다.
     * 2. **뷰 경로 반환**:
     *    - `authTrip/authTripModal`라는 이름의 템플릿 경로를 반환하여 모달 페이지를 렌더링합니다.
     */
    @GetMapping("/authTrip/authTripmodal")
    public String showAuthTripModal() {
        return "authTrip/authTripModal"; // 템플릿 경로에 맞게 수정
    }
}