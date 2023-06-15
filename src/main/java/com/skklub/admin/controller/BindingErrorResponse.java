package com.skklub.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingErrorResponse {
    private String url;
    private String exception;
    private List<ErrorDetail> errorDetails = new ArrayList<>();

    public static BindingErrorResponse fromException(BindException bindException, HttpServletRequest request) {
        List<ErrorDetail> errorDetails = bindException.getBindingResult().getFieldErrors().stream()
                .map(ErrorDetail::new)
                .collect(Collectors.toList());
        return BindingErrorResponse.builder()
                .errorDetails(errorDetails)
                .exception(bindException.getClass().toString())
                .url(request.getRequestURI())
                .build();
    }
}
