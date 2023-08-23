package com.skklub.admin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Supplier;

@AllArgsConstructor
@Getter
public class AuthException extends RuntimeException{
    private ErrorCode errorCode;
    private String message;
}
