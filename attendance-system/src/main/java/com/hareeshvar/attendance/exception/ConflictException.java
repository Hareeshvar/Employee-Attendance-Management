package com.hareeshvar.attendance.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseApplicationException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.CONFLICT);
    }
}
