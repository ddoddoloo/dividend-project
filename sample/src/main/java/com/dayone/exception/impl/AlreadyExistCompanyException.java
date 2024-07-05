package com.dayone.exception.impl;

import com.dayone.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyExistCompanyException extends AbstractException {

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "이미 정보를 보유하고 있는 회사명 입니다.";
    }
}