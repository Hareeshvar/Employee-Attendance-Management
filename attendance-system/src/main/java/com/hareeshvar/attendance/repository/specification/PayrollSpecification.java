package com.hareeshvar.attendance.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hareeshvar.attendance.entity.Payroll;
import com.hareeshvar.attendance.enums.RoleName;

import jakarta.persistence.criteria.Predicate;

public class PayrollSpecification {

    public static Specification<Payroll> filterPayrolls(
            String search,
            String month,
            Integer year,
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
                predicates.add(cb.or(firstName, lastName, username, email));
            }

            // Month filter
            if (StringUtils.hasText(month)) {
                predicates.add(cb.equal(cb.lower(root.get("month")), month.trim().toLowerCase()));
            }

            // Year filter
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
