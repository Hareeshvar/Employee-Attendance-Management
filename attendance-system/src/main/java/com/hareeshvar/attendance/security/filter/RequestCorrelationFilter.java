package com.hareeshvar.attendance.security.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String MDC_REQUEST_ID_KEY = "requestId";
    public static final String ATTRIBUTE_REQUEST_ID = "CORRELATION_REQUEST_ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader(HEADER_REQUEST_ID);

        if (!StringUtils.hasText(requestId) || !isValidRequestId(requestId)) {
            requestId = "req-" + UUID.randomUUID().toString();
        }

        request.setAttribute(ATTRIBUTE_REQUEST_ID, requestId);
        response.setHeader(HEADER_REQUEST_ID, requestId);
        MDC.put(MDC_REQUEST_ID_KEY, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    private boolean isValidRequestId(String requestId) {
        if (requestId.length() > 64) {
            return false;
        }
        if (requestId.contains("\r") || requestId.contains("\n")) {
            return false;
        }
        return requestId.matches("^[a-zA-Z0-9_.-]+$");
    }
}
