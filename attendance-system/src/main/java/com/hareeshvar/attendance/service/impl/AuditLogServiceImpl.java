package com.hareeshvar.attendance.service.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hareeshvar.attendance.dto.response.AuditLogResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.entity.AuditLog;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;
import com.hareeshvar.attendance.repository.AuditLogRepository;
import com.hareeshvar.attendance.repository.specification.AuditLogSpecification;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.security.trust-proxy:false}")
    private boolean trustProxy;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void log(AuditAction action, String entityType, String entityId, String description, AuditResult result, Map<String, Object> metadata) {
        saveAuditRecord(action, null, null, entityType, entityId, description, result, metadata);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void logSuccess(AuditAction action, String entityType, String entityId, String description, Map<String, Object> metadata) {
        log(action, entityType, entityId, description, AuditResult.SUCCESS, metadata);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(AuditAction action, String entityType, String entityId, String description, Map<String, Object> metadata) {
        saveAuditRecord(action, null, null, entityType, entityId, description, AuditResult.FAILURE, metadata);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDenied(AuditAction action, String entityType, String entityId, String description, Map<String, Object> metadata) {
        saveAuditRecord(action, null, null, entityType, entityId, description, AuditResult.DENIED, metadata);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSecurityEvent(AuditAction action, String actorUsername, String actorRole, String entityType, String entityId, String description, AuditResult result, Map<String, Object> metadata) {
        saveAuditRecord(action, actorUsername, actorRole, entityType, entityId, description, result, metadata);
    }

    private void saveAuditRecord(
            AuditAction action,
            String explicitActorUsername,
            String explicitActorRole,
            String entityType,
            String entityId,
            String description,
            AuditResult result,
            Map<String, Object> metadata
    ) {
        try {
            Long actorUserId = null;
            String actorUsername = explicitActorUsername;
            String actorRole = explicitActorRole;

            if (!StringUtils.hasText(actorUsername)) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                    actorUserId = userDetails.getUserId();
                    actorUsername = userDetails.getUsername();
                    actorRole = userDetails.getRole() != null ? userDetails.getRole().name() : "UNKNOWN";
                } else if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String principalStr) {
                    actorUsername = principalStr;
                    actorRole = "SYSTEM";
                } else {
                    actorUsername = "ANONYMOUS";
                    actorRole = "SYSTEM";
                }
            }

            if (!StringUtils.hasText(actorRole)) {
                actorRole = "SYSTEM";
            }

            String ipAddress = extractClientIp();
            String userAgent = extractUserAgent();
            String metadataJson = serializeMetadata(metadata);

            AuditLog auditLog = AuditLog.builder()
                    .actorUserId(actorUserId)
                    .actorUsername(actorUsername)
                    .actorRole(actorRole)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .result(result)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .metadata(metadataJson)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to persist audit log entry for action: {}", action, e);
        }
    }

    private String extractClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (trustProxy) {
                    String xForwardedFor = request.getHeader("X-Forwarded-For");
                    if (StringUtils.hasText(xForwardedFor)) {
                        return xForwardedFor.split(",")[0].trim();
                    }
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Unable to extract client IP", e);
        }
        return "127.0.0.1";
    }

    private String extractUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ua = request.getHeader("User-Agent");
                if (StringUtils.hasText(ua)) {
                    return ua.length() > 255 ? ua.substring(0, 255) : ua;
                }
            }
        } catch (Exception e) {
            log.debug("Unable to extract User-Agent", e);
        }
        return "UNKNOWN";
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("Failed to serialize audit log metadata", e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponseDTO> getAuditLogs(
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
    ) {
        int pageNumber = Math.max(0, page);
        int pageSize = Math.min(Math.max(1, size), 100);

        String sortProperty = StringUtils.hasText(sortBy) ? sortBy : "timestamp";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortProperty);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<AuditLog> auditPage = auditLogRepository.findAll(
                AuditLogSpecification.filterAuditLogs(action, entityType, actorUsername, result, startDate, endDate, search),
                pageable
        );

        Page<AuditLogResponseDTO> dtoPage = auditPage.map(this::mapToDTO);
        return PageResponse.from(dtoPage);
    }

    private AuditLogResponseDTO mapToDTO(AuditLog entity) {
        return AuditLogResponseDTO.builder()
                .id(entity.getId())
                .actorUserId(entity.getActorUserId())
                .actorUsername(entity.getActorUsername())
                .actorRole(entity.getActorRole())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .description(entity.getDescription())
                .timestamp(entity.getTimestamp())
                .result(entity.getResult())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .metadata(entity.getMetadata())
                .build();
    }
}
