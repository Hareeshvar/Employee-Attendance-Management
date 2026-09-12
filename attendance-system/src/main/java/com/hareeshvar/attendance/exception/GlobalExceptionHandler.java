package com.hareeshvar.attendance.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.hareeshvar.attendance.security.filter.RequestCorrelationFilter;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<ErrorResponse> handleBaseApplicationException(BaseApplicationException ex, WebRequest request) {
        log.warn("Application exception [code={}]: {}", ex.getErrorCode(), ex.getMessage());
        return buildErrorResponse(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String message = "Validation failed for " + fieldErrors.size() + " field(s): " + details;
        log.warn("Validation failure: {}", message);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, message, request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_JSON, "Invalid request payload or date format", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation: ", ex);
        String causeMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        if (causeMsg != null && (causeMsg.toLowerCase().contains("duplicate") || causeMsg.toLowerCase().contains("unique"))) {
            return buildErrorResponse(HttpStatus.CONFLICT, ErrorCode.RESOURCE_ALREADY_EXISTS, "Resource with provided key already exists", request, null);
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.DATA_INTEGRITY_VIOLATION, "Database constraint violation or invalid parameter", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, "Access Denied: You do not have permission to perform this operation or access this resource", request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        String msg = ex.getMessage() != null ? ex.getMessage() : "Authentication failed";
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, msg, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unhandled internal server exception [reqId={}]: ", MDC.get(RequestCorrelationFilter.MDC_REQUEST_ID_KEY), ex);
        String msg = "An unexpected error occurred. Please contact support with Request ID.";
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, msg, request, null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            WebRequest request,
            Map<String, String> fieldErrors
    ) {
        String path = null;
        if (request instanceof ServletWebRequest servletWebRequest) {
            path = servletWebRequest.getRequest().getRequestURI();
        }

        String requestId = MDC.get(RequestCorrelationFilter.MDC_REQUEST_ID_KEY);

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .requestId(requestId)
                .fieldErrors(fieldErrors)
                .build();

        return new ResponseEntity<>(error, status);
    }
}