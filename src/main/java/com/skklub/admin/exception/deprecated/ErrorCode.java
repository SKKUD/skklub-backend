package com.skklub.admin.exception.deprecated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@AllArgsConstructor
@Getter
public enum ErrorCode  {
    USERNAME_DUPLICATED(HttpStatus.CONFLICT,""),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,""),
    NO_AUTHORITY(HttpStatus.BAD_REQUEST,""),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,""),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED,""),
    NO_AUTHORIZED_USER(HttpStatus.UNAUTHORIZED,""),
    //requestTo 겸증
    WRONG_REQUEST(HttpStatus.BAD_REQUEST,""),

    //
    INVALID_AUTHORITY(HttpStatus.BAD_REQUEST,"");


    private HttpStatus httpStatus;
    private String message;
}
