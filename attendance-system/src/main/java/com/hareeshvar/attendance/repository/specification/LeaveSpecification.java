package com.hareeshvar.attendance.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hareeshvar.attendance.entity.Leave;
import com.hareeshvar.attendance.enums.LeaveStatus;
import com.hareeshvar.attendance.enums.LeaveType;
import com.hareeshvar.attendance.enums.RoleName;

import jakarta.persistence.criteria.Predicate;

public class LeaveSpecification {

    public static Specification<Leave> filterLeaves(
            String search,
            LeaveStatus status,
            LeaveType leaveType,
            RoleName currentUserRole,
            Long currentUserDeptId,
            Long currentUserId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // RBAC scoping
            if (currentUserRole == RoleName.MANAGER) {
                if (currentUserDeptId != null) {
                    predicates.add(cb.equal(root.get("user").get("department").get("departmentId"), currentUserDeptId));
                } else {
                    predicates.add(cb.disjunction());
                }
            } else if (currentUserRole == RoleName.EMPLOYEE) {
                predicates.add(cb.equal(root.get("user").get("userId"), currentUserId));
            }

            // Search filter
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate firstName = cb.like(cb.lower(root.get("user").get("firstName")), searchPattern);
                Predicate lastName = cb.like(cb.lower(root.get("user").get("lastName")), searchPattern);
                Predicate username = cb.like(cb.lower(root.get("user").get("username")), searchPattern);
                Predicate email = cb.like(cb.lower(root.get("user").get("email")), searchPattern);
                Predicate reason = cb.like(cb.lower(root.get("reason")), searchPattern);
                predicates.add(cb.or(firstName, lastName, username, email, reason));
            }

            // Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Leave type filter
            if (leaveType != null) {
                predicates.add(cb.equal(root.get("leaveType"), leaveType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
