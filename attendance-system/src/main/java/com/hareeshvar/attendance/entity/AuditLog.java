package com.hareeshvar.attendance.entity;

import java.time.LocalDateTime;

import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_timestamp", columnList = "timestamp DESC"),
    @Index(name = "idx_audit_logs_actor", columnList = "actorUsername"),
    @Index(name = "idx_audit_logs_action", columnList = "action"),
    @Index(name = "idx_audit_logs_entity", columnList = "entityType, entityId"),
    @Index(name = "idx_audit_logs_result", columnList = "result")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_username", nullable = false, length = 50)
    private String actorUsername;

    @Column(name = "actor_role", nullable = false, length = 20)
    private String actorRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 50)
    private String entityId;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private AuditResult result;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
