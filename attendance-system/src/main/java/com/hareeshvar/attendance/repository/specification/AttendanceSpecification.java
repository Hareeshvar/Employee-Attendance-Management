package com.hareeshvar.attendance.repository.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hareeshvar.attendance.entity.Attendance;
import com.hareeshvar.attendance.enums.AttendanceStatus;
import com.hareeshvar.attendance.enums.RoleName;

import jakarta.persistence.criteria.Predicate;

public class AttendanceSpecification {

    public static Specification<Attendance> filterAttendance(
            String search,
            AttendanceStatus status,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            RoleName currentUserRole,
            Long currentUserDeptId,
            Long currentUserId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // RBAC scoping
            if (currentUserRole == RoleName.MANAGER) {
                if (currentUserDeptId != null) {
                    predicates.add(cb.equal(root.get("department").get("departmentId"), currentUserDeptId));
                } else {
                    predicates.add(cb.disjunction());
                }
            } else if (currentUserRole == RoleName.EMPLOYEE) {
                predicates.add(cb.equal(root.get("user").get("userId"), currentUserId));
            }

            // Search filter (user firstName, lastName, username, email)
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate firstName = cb.like(cb.lower(root.get("user").get("firstName")), searchPattern);
                Predicate lastName = cb.like(cb.lower(root.get("user").get("lastName")), searchPattern);
                Predicate username = cb.like(cb.lower(root.get("user").get("username")), searchPattern);
                Predicate email = cb.like(cb.lower(root.get("user").get("email")), searchPattern);
                predicates.add(cb.or(firstName, lastName, username, email));
            }

            // Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Department filter
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("departmentId"), departmentId));
            }

            // Date range filter
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("attendanceDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("attendanceDate"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
