package com.skklub.admin.controller.error.exception;

public class NoMatchClubException extends RuntimeException {
    public NoMatchClubException() {
        super();
    }

    public NoMatchClubException(String message) {
        super(message);
    }

    public NoMatchClubException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMatchClubException(Throwable cause) {
        super(cause);
    }

    protected NoMatchClubException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
