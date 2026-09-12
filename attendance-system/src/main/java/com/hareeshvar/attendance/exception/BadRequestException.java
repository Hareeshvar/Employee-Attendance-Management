package com.hareeshvar.attendance.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseApplicationException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
    }
}