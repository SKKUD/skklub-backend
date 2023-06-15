package com.skklub.admin.controller.error.handler;

import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.error.handler.dto.BindingErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = ClubController.class)
public class ClubExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BindingErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(BindingErrorResponse.fromException(e, request));
    }
}
