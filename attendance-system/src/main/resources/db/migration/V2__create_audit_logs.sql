-- Migration V2: Create Audit Logs Table for Enterprise Audit System
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT NULL,
    actor_username VARCHAR(50) NOT NULL,
    actor_role VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NULL,
    description VARCHAR(500) NOT NULL,
    timestamp DATETIME(6) NOT NULL,
    result VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    metadata TEXT NULL,
    INDEX idx_audit_logs_timestamp (timestamp DESC),
    INDEX idx_audit_logs_actor (actor_username),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
