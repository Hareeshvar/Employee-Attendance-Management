package com.hareeshvar.attendance.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends BaseApplicationException {

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.RESOURCE_ALREADY_EXISTS);
    }
}