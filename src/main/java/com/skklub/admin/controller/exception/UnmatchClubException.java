package com.skklub.admin.controller.exception;

public class UnmatchClubException extends RuntimeException {
    public UnmatchClubException() {
        super();
    }

    public UnmatchClubException(String message) {
        super(message);
    }

    public UnmatchClubException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnmatchClubException(Throwable cause) {
        super(cause);
    }

    protected UnmatchClubException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
