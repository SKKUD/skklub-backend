package com.skklub.admin.exception.deprecated.error.exception;

public class ClubIdMisMatchException extends RuntimeException {
    public ClubIdMisMatchException() {
        super();
    }

    public ClubIdMisMatchException(String message) {
        super(message);
    }

    public ClubIdMisMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClubIdMisMatchException(Throwable cause) {
        super(cause);
    }

    protected ClubIdMisMatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
