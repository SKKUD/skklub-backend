package com.skklub.admin.exception.deprecated.error.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {
    private String field;
    private String given;
    private String reasonMessage;

    public ErrorDetail(FieldError fieldError) {
        field = fieldError.getField();
        given = fieldError.getRejectedValue() == null ? "" : fieldError.getRejectedValue().toString();
        reasonMessage = fieldError.getDefaultMessage();
    }
}
