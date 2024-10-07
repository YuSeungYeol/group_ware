package com.ware.spring;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            // 404 에러 처리
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }
            // 500 에러 처리
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            }
            // 403 에러 처리
            else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            }
        }
        // 기타 에러 처리
        return "error/general";
    }
}
