package com.hareeshvar.attendance.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseApplicationException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue), HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);
    }
}