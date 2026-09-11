package com.hareeshvar.attendance.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hareeshvar.attendance.dto.response.AuditLogResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;
import com.hareeshvar.attendance.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public PageResponse<AuditLogResponseDTO> getAuditLogs(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) AuditResult result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return auditLogService.getAuditLogs(
                action, entityType, actorUsername, result,
                startDate, endDate, search,
                page, size, sortBy, sortDir
        );
    }
}
