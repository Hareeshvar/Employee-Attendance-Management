package com.hareeshvar.attendance.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class BaseApplicationException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    public BaseApplicationException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public BaseApplicationException(String message, Throwable cause, HttpStatus status, ErrorCode errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
