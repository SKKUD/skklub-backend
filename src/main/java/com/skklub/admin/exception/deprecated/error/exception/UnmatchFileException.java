package com.skklub.admin.exception.deprecated.error.exception;

public class UnmatchFileException extends RuntimeException{
    public UnmatchFileException() {
        super();
    }

    public UnmatchFileException(String message) {
        super(message);
    }

    public UnmatchFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnmatchFileException(Throwable cause) {
        super(cause);
    }

    protected UnmatchFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
