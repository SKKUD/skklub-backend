package com.skklub.admin.exception.deprecated.error.handler.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String url;
    private String exception;
    private ErrorDetail errorDetail;

    public static ErrorResponse fromException(Exception e, HttpServletRequest request, ErrorDetail errorDetail) {
        return ErrorResponse.builder()
                .url(request.getRequestURI())
                .exception(e.getClass().getName())
                .errorDetail(errorDetail)
                .build();
    }
}
