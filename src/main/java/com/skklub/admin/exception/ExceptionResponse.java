package com.skklub.admin.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {
    private ExceptionCode exceptionCode;
    private LocalDateTime timeStamp;
    private String detailMessage;
    private Class exceptionClassName;

    public ExceptionResponse(RuntimeException exception) {
        this.exceptionCode = null;
        this.timeStamp = LocalDateTime.now();
        this.detailMessage = exception.getMessage();
        this.exceptionClassName = exception.getCause().getClass();
    }
}
