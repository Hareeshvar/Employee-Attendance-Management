package com.hareeshvar.attendance.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hareeshvar.attendance.dto.response.AuditLogResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.entity.AuditLog;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;
import com.hareeshvar.attendance.repository.AuditLogRepository;
import com.hareeshvar.attendance.service.impl.AuditLogServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = AuditLog.builder()
                .id(1L)
                .actorUserId(10L)
                .actorUsername("adminUser")
                .actorRole("ADMIN")
                .action(AuditAction.USER_CREATED)
                .entityType("USER")
                .entityId("42")
                .description("Created user john_doe")
                .timestamp(java.time.LocalDateTime.now())
                .result(AuditResult.SUCCESS)
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla/5.0")
                .metadata("{\"username\":\"john_doe\"}")
                .build();
    }

    @Test
    void testLogSuccess() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(sampleLog);

        auditLogService.logSuccess(
                AuditAction.USER_CREATED,
                "USER",
                "42",
                "Created user john_doe",
                Map.of("username", "john_doe")
        );

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testUntrustedClientForwardedForHeaderIsIgnored() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        try {
            when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195");
            when(request.getRemoteAddr()).thenReturn("192.168.1.50");
            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(sampleLog);

            auditLogService.logSuccess(
                    AuditAction.USER_CREATED,
                    "USER",
                    "42",
                    "Security test for untrusted IP",
                    Map.of("username", "john_doe")
            );

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(captor.capture());

            AuditLog savedLog = captor.getValue();
            // Default trustProxy is false, so X-Forwarded-For header MUST be ignored
            assertEquals("192.168.1.50", savedLog.getIpAddress());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void testGetAuditLogsPaginated() {
        Page<AuditLog> page = new PageImpl<>(java.util.List.of(sampleLog));
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<AuditLogResponseDTO> response = auditLogService.getAuditLogs(
                AuditAction.USER_CREATED, "USER", "adminUser", AuditResult.SUCCESS,
                null, null, null, 0, 10, "timestamp", "desc"
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("adminUser", response.getContent().get(0).getActorUsername());
        assertEquals(AuditAction.USER_CREATED, response.getContent().get(0).getAction());
        assertTrue(response.isFirst());
    }
}
