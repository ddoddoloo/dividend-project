package com.dayone.exception.impl;

import com.dayone.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class EmptyTickerException extends AbstractException {

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "잘못된 티커 정보 입니다.";
    }
}