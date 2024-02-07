package com.skklub.admin.exception.deprecated;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionManager {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runtimeExceptionHandler(RuntimeException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> authExceptionHandler(AuthException e){
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(e.getErrorCode().name()+" : "+e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<?> invalidTokenExceptionHandler(InvalidTokenException e){
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(e.getErrorCode().name()+" : "+e.getMessage());
    }

}
