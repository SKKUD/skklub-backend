package com.skklub.admin.exception.deprecated.error.exception;

public class MissingAliveClubException extends RuntimeException{
    public MissingAliveClubException() {
        super();
    }

    public MissingAliveClubException(String message) {
        super(message);
    }

    public MissingAliveClubException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingAliveClubException(Throwable cause) {
        super(cause);
    }

    protected MissingAliveClubException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
