package com.hareeshvar.attendance.dto.response;

import java.time.LocalDateTime;

import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponseDTO {
    private Long id;
    private Long actorUserId;
    private String actorUsername;
    private String actorRole;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String description;
    private LocalDateTime timestamp;
    private AuditResult result;
    private String ipAddress;
    private String userAgent;
    private String metadata;
}
