-- =============================================================================
-- V1__baseline_schema.sql
-- Smart Attendance & HR Management System Baseline Schema Migration
-- Compatible with MySQL 8.x and Spring Boot 3.5 + Hibernate JPA Mapping
-- =============================================================================

-- 1. ROLES
CREATE TABLE IF NOT EXISTS roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. DEPARTMENTS
CREATE TABLE IF NOT EXISTS departments (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    department_code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(255),
    status VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. DESIGNATIONS
CREATE TABLE IF NOT EXISTS designations (
    designation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    designation_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(300),
    created_at DATETIME(6),
    updated_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. SHIFTS
CREATE TABLE IF NOT EXISTS shifts (
    shift_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_name VARCHAR(50) NOT NULL UNIQUE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    working_hours INT NOT NULL,
    description VARCHAR(300),
    created_at DATETIME(6),
    updated_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. USERS
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    gender VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    designation_id BIGINT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments(department_id),
    CONSTRAINT fk_users_designation FOREIGN KEY (designation_id) REFERENCES designations(designation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. EMPLOYEE SHIFTS
CREATE TABLE IF NOT EXISTS employee_shifts (
    employee_shift_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    shift_id BIGINT NOT NULL,
    effective_date DATE NOT NULL,
    status VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_emp_shifts_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_emp_shifts_shift FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. ATTENDANCE
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time TIME NOT NULL,
    check_out_time TIME,
    working_hours DOUBLE,
    status VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_attendance_department FOREIGN KEY (department_id) REFERENCES departments(department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. LEAVE REQUESTS
CREATE TABLE IF NOT EXISTS leave_requests (
    leave_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    leave_type VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(255) NOT NULL,
    total_days INT,
    leave_type_id BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_leave_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. PAYROLLS
CREATE TABLE IF NOT EXISTS payrolls (
    payroll_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    month VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    basic_salary DOUBLE NOT NULL,
    bonus DOUBLE NOT NULL,
    deduction DOUBLE NOT NULL,
    net_salary DOUBLE NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_payrolls_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. REPORTS
CREATE TABLE IF NOT EXISTS reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_name VARCHAR(150) NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    generated_date DATETIME(6) NOT NULL,
    generated_by VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at DATETIME(6),
    updated_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. NOTIFICATIONS
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BIT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
