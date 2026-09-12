-- =============================================================================
-- V4__attendance_intelligence.sql
-- Phase 6 Attendance Intelligence Schema Upgrade
-- Non-destructive additions to shifts and attendance tables
-- =============================================================================

-- 1. Add grace_minutes to shifts table
ALTER TABLE shifts 
    ADD COLUMN grace_minutes INT NOT NULL DEFAULT 15;

-- 2. Add intelligence calculation columns to attendance table
ALTER TABLE attendance 
    ADD COLUMN shift_id BIGINT NULL,
    ADD COLUMN late_minutes INT NOT NULL DEFAULT 0,
    ADD COLUMN early_departure_minutes INT NOT NULL DEFAULT 0,
    ADD COLUMN working_minutes INT NOT NULL DEFAULT 0,
    ADD COLUMN overtime_minutes INT NOT NULL DEFAULT 0,
    ADD COLUMN exceptions_json VARCHAR(255) NOT NULL DEFAULT '[]';

-- 3. Add Foreign Key for shift_id in attendance table referencing shifts(shift_id)
ALTER TABLE attendance 
    ADD CONSTRAINT fk_attendance_shift FOREIGN KEY (shift_id) REFERENCES shifts(shift_id);
