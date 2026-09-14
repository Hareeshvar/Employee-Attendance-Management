-- =============================================================================
-- V5__geofenced_workplaces.sql
-- Phase 7: Secure Geofenced Attendance Schema
-- Compatible with MySQL 8.x and Spring Boot 3.5 + Hibernate JPA Mapping
-- Strictly Non-Destructive: Preserves V1, V2, V3, and V4 baseline schemas
-- =============================================================================

-- 1. WORKPLACES TABLE
-- Stores physical and virtual workplace coordinates and operational geofence parameters
CREATE TABLE IF NOT EXISTS workplaces (
    workplace_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    radius_meters DOUBLE NOT NULL DEFAULT 100.0,
    max_accuracy_meters DOUBLE NOT NULL DEFAULT 100.0,
    is_active BIT(1) NOT NULL DEFAULT 1,
    description VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_workplaces_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. EMPLOYEE WORKPLACE ASSIGNMENTS TABLE
-- History-preserving: No UNIQUE(user_id, workplace_id) to allow non-overlapping re-assignment over time
CREATE TABLE IF NOT EXISTS employee_workplaces (
    employee_workplace_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    workplace_id BIGINT NOT NULL,
    is_primary BIT(1) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_emp_workplaces_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_emp_workplaces_workplace FOREIGN KEY (workplace_id) REFERENCES workplaces(workplace_id),
    INDEX idx_emp_wp_lookup (user_id, status, effective_from, effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. ATTENDANCE LOCATION VERIFICATIONS TABLE
-- Decoupled check-in vs check-out verification evidence without raw GPS coordinate storage
CREATE TABLE IF NOT EXISTS attendance_location_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_id BIGINT NOT NULL,
    punch_type VARCHAR(20) NOT NULL,
    workplace_id BIGINT NULL,
    distance_meters DOUBLE NULL,
    location_accuracy_meters DOUBLE NULL,
    location_verified BIT(1) NOT NULL DEFAULT 0,
    verification_method VARCHAR(30) NOT NULL,
    verified_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_alv_attendance FOREIGN KEY (attendance_id) REFERENCES attendance(attendance_id) ON DELETE CASCADE,
    CONSTRAINT fk_alv_workplace FOREIGN KEY (workplace_id) REFERENCES workplaces(workplace_id),
    CONSTRAINT uk_alv_attendance_punch UNIQUE (attendance_id, punch_type),
    INDEX idx_alv_workplace (workplace_id),
    INDEX idx_alv_verified_at (verified_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
