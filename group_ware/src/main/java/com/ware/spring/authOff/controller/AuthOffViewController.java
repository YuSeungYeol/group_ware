package com.ware.spring.authOff.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ware.spring.authOff.domain.AuthOffDto;
import com.ware.spring.authOff.service.AuthOffService;

@Controller
public class AuthOffViewController {

    private final AuthOffService authOffService;

    @Autowired
    public AuthOffViewController(AuthOffService authOffService) {
        this.authOffService = authOffService;
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **휴가 신청서 목록 조회**: 데이터베이스에서 `AuthOffDto` 리스트를 조회하여 모델에 추가.
     * - **목록 템플릿 반환**: 조회된 목록 데이터를 화면에 표시하기 위한 템플릿 반환.
     *
     * ### 기술
     * - **Spring MVC**: `Model` 객체를 통해 데이터를 뷰로 전달.
     * - **Service Layer**: `authOffService.selectAuthOffList()`를 통해 목록을 조회.
     * - **Thymeleaf**: 조회된 `AuthOffDto` 목록을 템플릿에서 활용하여 화면에 출력.
     *
     * ### 구현
     * - `authOffService.selectAuthOffList()` 메서드를 호출하여 휴가 신청서 목록을 가져옴.
     * - 가져온 목록을 `model` 객체에 추가하여 뷰에서 사용하도록 설정.
     * - 템플릿 경로를 지정하여 해당 템플릿을 반환.
     */
    @GetMapping("/authOffList")
    public String selectAuthOffList(Model model) {
        List<AuthOffDto> resultList = authOffService.selectAuthOffList();
        model.addAttribute("resultList", resultList);
        return "authorization/authorizationOffList"; // 템플릿 경로에 맞게 수정
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **휴가 신청서 상세 페이지 조회**: 휴가 신청서의 상세 페이지로 이동하는 기능.
     * - **상세 템플릿 반환**: 휴가 신청서의 상세 내용을 보여줄 템플릿을 반환.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`을 통해 클라이언트 요청을 처리하고, 상세 페이지 템플릿을 반환.
     * - **Thymeleaf**: 상세 페이지를 위한 템플릿 경로 설정.
     *
     * ### 구현
     * - `/authOff` URL로 요청이 들어오면, `authOff/authOffView` 템플릿을 반환하여 휴가 신청서의 상세 정보를 보여주는 페이지로 이동.
     * - 클라이언트는 해당 페이지에서 상세 정보를 확인할 수 있음.
     */
    @GetMapping("/authOff")
    public String selectAuthOff() {
        return "authOff/authOffView";
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **휴가 신청서 작성 페이지 이동**: 휴가 신청서를 작성할 수 있는 페이지로 이동하는 기능.
     * - **작성 템플릿 반환**: 휴가 신청서 작성 페이지를 위한 템플릿을 반환.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`을 통해 클라이언트 요청을 처리하고, 작성 페이지 템플릿을 반환.
     * - **Thymeleaf**: 작성 페이지를 위한 템플릿 경로 설정.
     *
     * ### 구현
     * - `/authOff/authOffCreate` URL로 요청이 들어오면, `authorization/authorizationOff` 템플릿을 반환하여 휴가 신청서를 작성하는 페이지로 이동.
     * - 클라이언트는 해당 페이지에서 휴가 신청서를 작성할 수 있음.
     */
    @GetMapping("/authOff/authOffCreate")
    public String createAuthOffPage() {
        return "authorization/authorizationOff";
    }

    /**
     * ## 기능 및 기술 요약
     *
     * ### 기능
     * - **휴가 신청서 모달 페이지 이동**: 휴가 신청서 모달을 띄울 수 있는 페이지로 이동하는 기능.
     * - **모달 템플릿 반환**: 휴가 신청서 모달을 위한 템플릿을 반환.
     *
     * ### 기술
     * - **Spring MVC**: `@GetMapping`을 통해 클라이언트 요청을 처리하고, 모달 템플릿을 반환.
     * - **Thymeleaf**: 모달 페이지를 위한 템플릿 경로 설정.
     *
     * ### 구현
     * - `/authOff/authOffmodal` URL로 요청이 들어오면, `authOff/authOffModal` 템플릿을 반환하여 휴가 신청서 모달 페이지를 띄움.
     * - 클라이언트는 해당 모달을 통해 휴가 신청서를 빠르게 작성할 수 있음.
     */
    @GetMapping("/authOff/authOffmodal")
    public String showAuthOffModal() {
        return "authOff/authOffModal"; 
    }
}