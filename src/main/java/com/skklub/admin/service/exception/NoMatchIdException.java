package com.skklub.admin.service.exception;

import com.skklub.admin.exception.ClientSideException;

public class NoMatchIdException extends ClientSideException {
    public NoMatchIdException(Long invalidId) {
        super("제공된 ID값(" + invalidId + ")과 일치하는 데이터가 존재하지 않습니다");
    }
}
