package com.skklub.admin;

import com.skklub.admin.exception.ClientSideException;
import com.skklub.admin.exception.ExceptionResponse;
import com.skklub.admin.exception.ServerSideException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 입력값이 잘못되어 예외가 터진 경우
     */
    @ExceptionHandler(ClientSideException.class)
    public ResponseEntity<ExceptionResponse> clientSideException(ClientSideException clientSideException, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(clientSideException);
        return ResponseEntity
                .badRequest()
                .body(exceptionResponse);
    }

    /**
     * 서버 내부 문제로 예외가 터진 경우
     */
    @ExceptionHandler(ServerSideException.class)
    public ResponseEntity<ExceptionResponse> serverSideException(ServerSideException serverSideException, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(serverSideException);
        return ResponseEntity
                .internalServerError()
                .body(exceptionResponse);
    }

    /**
     * Spring Bean Validation 관련 예외
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ExceptionResponse> bindException(BindException bindException, HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(null);
    }

    /**
     * Spring MVC Controller 파라미터 매칭 예외
     */
    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ExceptionResponse> servletException(ServletException servletException, HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(null);
    }
}
