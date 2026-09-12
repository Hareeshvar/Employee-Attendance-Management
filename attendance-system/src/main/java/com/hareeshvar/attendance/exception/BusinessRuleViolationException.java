package com.hareeshvar.attendance.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleViolationException extends BaseApplicationException {

    public BusinessRuleViolationException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.BUSINESS_RULE_VIOLATION);
    }
}
