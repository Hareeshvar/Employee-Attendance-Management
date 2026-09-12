package com.hareeshvar.attendance.security.jwt;

import java.io.IOException;
import java.time.LocalDateTime;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hareeshvar.attendance.exception.ErrorCode;
import com.hareeshvar.attendance.exception.ErrorResponse;
import com.hareeshvar.attendance.security.filter.RequestCorrelationFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        String requestId = (String) request.getAttribute(RequestCorrelationFilter.ATTRIBUTE_REQUEST_ID);
        if (requestId == null) {
            requestId = MDC.get(RequestCorrelationFilter.MDC_REQUEST_ID_KEY);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .errorCode(ErrorCode.UNAUTHORIZED)
                .message(authException != null && authException.getMessage() != null ? authException.getMessage() : "Unauthorized access")
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}