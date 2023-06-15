package com.skklub.admin.controller.error.handler;

import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.error.exception.NoMatchClubException;
import com.skklub.admin.controller.error.handler.dto.BindingErrorResponse;
import com.skklub.admin.controller.error.handler.dto.ErrorDetail;
import com.skklub.admin.controller.error.handler.dto.ErrorResponse;
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

    @ExceptionHandler(NoMatchClubException.class)
    public ResponseEntity<ErrorResponse> noMatchClubException(NoMatchClubException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("clubId")
                .given("path variable")
                .reasonMessage("해당 ID값을 갖는 club이 존재하지 않습니다")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }
}
