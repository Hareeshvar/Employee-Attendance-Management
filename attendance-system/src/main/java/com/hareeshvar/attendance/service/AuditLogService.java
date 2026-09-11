package com.hareeshvar.attendance.service;

import java.time.LocalDateTime;
import java.util.Map;

import com.hareeshvar.attendance.dto.response.AuditLogResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;

public interface AuditLogService {

    void log(AuditAction action, String entityType, String entityId, String description, AuditResult result, Map<String, Object> metadata);

    void logSuccess(AuditAction action, String entityType, String entityId, String description, Map<String, Object> metadata);

    void logFailure(AuditAction action, String entityType, String entityId, String description, Map<String, Object> metadata);

    void logDenied(AuditAction action, String entityType, String entityId, String description, Map<String, Object> metadata);

    void logSecurityEvent(AuditAction action, String actorUsername, String actorRole, String entityType, String entityId, String description, AuditResult result, Map<String, Object> metadata);

    PageResponse<AuditLogResponseDTO> getAuditLogs(
            AuditAction action,
            String entityType,
            String actorUsername,
            AuditResult result,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
