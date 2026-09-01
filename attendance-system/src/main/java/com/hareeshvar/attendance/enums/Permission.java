package com.hareeshvar.attendance.enums;

import java.util.Set;

public enum Permission {

    // User permissions
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,

    // Role & Org permissions
    ROLE_READ,
    ROLE_MANAGE,
    DEPARTMENT_MANAGE,
    DESIGNATION_MANAGE,
    SHIFT_MANAGE,

    // Attendance permissions
    ATTENDANCE_READ_ALL,
    ATTENDANCE_READ_SELF,
    ATTENDANCE_MANAGE,
    ATTENDANCE_CHECKIN_SELF,
    ATTENDANCE_CHECKOUT_SELF,

    // Leave permissions
    LEAVE_READ_ALL,
    LEAVE_READ_SELF,
    LEAVE_CREATE_SELF,
    LEAVE_CANCEL_SELF,
    LEAVE_MANAGE,
    LEAVE_APPROVE,

    // Payroll permissions
    PAYROLL_READ_ALL,
    PAYROLL_READ,
    PAYROLL_READ_SELF,
    PAYROLL_MANAGE,

    // Report permissions
    REPORT_READ_ALL,
    REPORT_READ_HR,
    TEAM_REPORT_READ,

    // Notification permissions
    NOTIFICATION_MANAGE,
    NOTIFICATION_READ_SELF,

    // Team permissions (Manager)
    TEAM_READ,
    TEAM_ATTENDANCE_READ,
    TEAM_LEAVE_READ,
    TEAM_LEAVE_APPROVE,

    // Self profile permissions
    PROFILE_READ_SELF,
    PROFILE_UPDATE_SELF;

    public static Set<Permission> getPermissionsForRole(RoleName roleName) {
        if (roleName == null) {
            return Set.of();
        }

        switch (roleName) {
            case ADMIN:
                return Set.of(
                        USER_READ, USER_CREATE, USER_UPDATE, USER_DELETE,
                        ROLE_READ, ROLE_MANAGE, DEPARTMENT_MANAGE, DESIGNATION_MANAGE, SHIFT_MANAGE,
                        ATTENDANCE_READ_ALL, ATTENDANCE_READ_SELF, ATTENDANCE_MANAGE, ATTENDANCE_CHECKIN_SELF, ATTENDANCE_CHECKOUT_SELF,
                        LEAVE_READ_ALL, LEAVE_READ_SELF, LEAVE_CREATE_SELF, LEAVE_CANCEL_SELF, LEAVE_MANAGE, LEAVE_APPROVE,
                        PAYROLL_READ_ALL, PAYROLL_READ, PAYROLL_READ_SELF, PAYROLL_MANAGE,
                        REPORT_READ_ALL, REPORT_READ_HR, TEAM_REPORT_READ,
                        NOTIFICATION_MANAGE, NOTIFICATION_READ_SELF,
                        TEAM_READ, TEAM_ATTENDANCE_READ, TEAM_LEAVE_READ, TEAM_LEAVE_APPROVE,
                        PROFILE_READ_SELF, PROFILE_UPDATE_SELF
                );

            case HR:
                return Set.of(
                        USER_READ, USER_CREATE, USER_UPDATE,
                        DEPARTMENT_MANAGE, DESIGNATION_MANAGE, SHIFT_MANAGE,
                        ATTENDANCE_READ_ALL, ATTENDANCE_READ_SELF, ATTENDANCE_MANAGE, ATTENDANCE_CHECKIN_SELF, ATTENDANCE_CHECKOUT_SELF,
                        LEAVE_READ_ALL, LEAVE_READ_SELF, LEAVE_CREATE_SELF, LEAVE_CANCEL_SELF, LEAVE_APPROVE,
                        PAYROLL_READ, PAYROLL_READ_SELF, PAYROLL_MANAGE,
                        REPORT_READ_HR,
                        NOTIFICATION_MANAGE, NOTIFICATION_READ_SELF,
                        PROFILE_READ_SELF, PROFILE_UPDATE_SELF
                );

            case MANAGER:
                return Set.of(
                        TEAM_READ, TEAM_ATTENDANCE_READ, TEAM_LEAVE_READ, TEAM_LEAVE_APPROVE, TEAM_REPORT_READ,
                        ATTENDANCE_READ_SELF, ATTENDANCE_CHECKIN_SELF, ATTENDANCE_CHECKOUT_SELF,
                        LEAVE_CREATE_SELF, LEAVE_READ_SELF, LEAVE_CANCEL_SELF,
                        PAYROLL_READ_SELF,
                        NOTIFICATION_READ_SELF,
                        PROFILE_READ_SELF, PROFILE_UPDATE_SELF
                );

            case EMPLOYEE:
            default:
                return Set.of(
                        PROFILE_READ_SELF, PROFILE_UPDATE_SELF,
                        ATTENDANCE_READ_SELF, ATTENDANCE_CHECKIN_SELF, ATTENDANCE_CHECKOUT_SELF,
                        LEAVE_CREATE_SELF, LEAVE_READ_SELF, LEAVE_CANCEL_SELF,
                        PAYROLL_READ_SELF,
                        NOTIFICATION_READ_SELF
                );
        }
    }
}
