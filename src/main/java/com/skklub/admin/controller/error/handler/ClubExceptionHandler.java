package com.skklub.admin.controller.error.handler;

import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.RecruitController;
import com.skklub.admin.controller.error.exception.*;
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
@RestControllerAdvice(basePackageClasses = {ClubController.class, RecruitController.class})
public class ClubExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BindingErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(BindingErrorResponse.fromException(e, request));
    }

    @ExceptionHandler(ClubIdMisMatchException.class)
    public ResponseEntity<ErrorResponse> noMatchClubException(ClubIdMisMatchException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("clubId")
                .given("path variable")
                .reasonMessage("해당 ID값을 갖는 club이 존재하지 않습니다")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }

    @ExceptionHandler(ClubNameMisMatchException.class)
    public ResponseEntity<ErrorResponse> noMatchClubException(ClubNameMisMatchException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("name")
                .given(request.getParameter("name"))
                .reasonMessage("해당 이름과 일치하는 club이 존재하지 않습니다")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }

    @ExceptionHandler(AlreadyRecruitingException.class)
    public ResponseEntity<ErrorResponse> alreadyRecruitingException(AlreadyRecruitingException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("clubId")
                .given("path variable")
                .reasonMessage("이미 모집 정보가 등록된 club입니다")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }

    @ExceptionHandler(RecruitIdMisMatchException.class)
    public ResponseEntity<ErrorResponse> recruitIdMisMatchException(RecruitIdMisMatchException e, HttpServletRequest req) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("recruitId")
                .given("path variable")
                .reasonMessage("해당 아이디와 일치하는 Recruit 정보가 없습니다")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, req, errorDetail));
    }

    @ExceptionHandler(AllTimeRecruitTimeFormattingException.class)
    public ResponseEntity<ErrorResponse> allTimeRecruitTimeFormattingException(AllTimeRecruitTimeFormattingException e, HttpServletRequest req) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("recruitStartAt, recruitEndAt")
                .given(req.getParameter("recruitStartAt") + ", " + req.getParameter("recruitEndAt"))
                .reasonMessage("둘다 NULL이거나 둘다 NULL이 아니어야합니다")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, req, errorDetail));
    }
}
