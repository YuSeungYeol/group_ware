package com.ware.spring.security.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String xhrHeader = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(xhrHeader);

        if (isAjax) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\": \"로그인 성공\"}");
        } else {
            response.sendRedirect("/");
        }
    }
}
