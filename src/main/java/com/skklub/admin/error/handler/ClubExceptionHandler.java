package com.skklub.admin.error.handler;

import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.NoticeController;
import com.skklub.admin.controller.RecruitController;
import com.skklub.admin.error.exception.*;
import com.skklub.admin.error.handler.dto.BindingErrorResponse;
import com.skklub.admin.error.handler.dto.ErrorDetail;
import com.skklub.admin.error.handler.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice(basePackageClasses = {ClubController.class, RecruitController.class, NoticeController.class})
public class ClubExceptionHandler {
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BindingErrorResponse> methodArgumentNotValidException(BindException e, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(BindingErrorResponse.fromException(e, request));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field(e.getParameterName())
                .given("none")
                .reasonMessage("명시된 field는 필수 입니다.")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> missingServletRequestPartException(MissingServletRequestPartException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field(e.getRequestPartName())
                .given("none")
                .reasonMessage("명시된 field는 필수 입니다.")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
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

    @ExceptionHandler(ActivityImageMisMatchException.class)
    public ResponseEntity<ErrorResponse> activityImageMisMatchException(ActivityImageMisMatchException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("clubId, activityImageName")
                .given(request.getParameter("clubId") + ", " + request.getParameter("activityImageName"))
                .reasonMessage("해당 클럽 아이디에 매칭하는 활동 사진명이 존재하지 않습니다")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }

    @ExceptionHandler(MissingAliveClubException.class)
    public ResponseEntity<ErrorResponse> doubleClubDeletionException(MissingAliveClubException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("clubId")
                .given("path variable")
                .reasonMessage("이미 삭제된 동아리 입니다.")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }

    @ExceptionHandler(MissingDeletedClubException.class)
    public ResponseEntity<ErrorResponse> alreadyAliveClubException(MissingDeletedClubException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("clubId")
                .given("path variable")
                .reasonMessage("이미 살아있는 동아리 입니다.")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }

    @ExceptionHandler(NotRecruitingException.class)
    public ResponseEntity<ErrorResponse> notRecruitingException(NotRecruitingException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("clubId")
                .given("path variable")
                .reasonMessage("모집 정보가 존재하지 않는 동아리 입니다.")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }

    @ExceptionHandler(NoticeIdMisMatchException.class)
    public ResponseEntity<ErrorResponse> noticeIdMisMatchException(NoticeIdMisMatchException e, HttpServletRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field("noticeId")
                .given("path variable")
                .reasonMessage("해당 ID값을 갖는 Notice가 존재하지 않습니다")
                .build();
        return ResponseEntity.badRequest().body(ErrorResponse.fromException(e, request, errorDetail));
    }
}
