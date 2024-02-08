package com.skklub.admin.exception;

public class ServerSideException extends RuntimeException {
    public ServerSideException() {
    }

    public ServerSideException(String message) {
        super(message);
    }

    public ServerSideException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerSideException(Throwable cause) {
        super(cause);
    }

    public ServerSideException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
