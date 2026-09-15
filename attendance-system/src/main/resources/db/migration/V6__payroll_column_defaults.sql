-- =============================================================================
-- V6__payroll_column_defaults.sql
-- Fix payroll column constraints and guarantee defaults for allowances and overtime_pay
-- =============================================================================

ALTER TABLE payrolls 
    MODIFY COLUMN allowances DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    MODIFY COLUMN overtime_pay DECIMAL(10,2) NOT NULL DEFAULT 0.00;
