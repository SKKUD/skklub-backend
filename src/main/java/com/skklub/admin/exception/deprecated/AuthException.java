package com.skklub.admin.exception.deprecated;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthException extends RuntimeException{
    private ErrorCode errorCode;
    private String message;
}
