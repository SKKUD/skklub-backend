package com.skklub.admin.error.exception;

public class DoubleClubDeletionException extends RuntimeException{
    public DoubleClubDeletionException() {
        super();
    }

    public DoubleClubDeletionException(String message) {
        super(message);
    }

    public DoubleClubDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DoubleClubDeletionException(Throwable cause) {
        super(cause);
    }

    protected DoubleClubDeletionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
