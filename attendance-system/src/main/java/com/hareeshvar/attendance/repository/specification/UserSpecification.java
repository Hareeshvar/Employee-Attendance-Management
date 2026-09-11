package com.hareeshvar.attendance.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.enums.UserStatus;

import jakarta.persistence.criteria.Predicate;

public class UserSpecification {

    public static Specification<User> filterUsers(
            String search,
            Long departmentId,
            UserStatus status,
            Long roleId,
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
                predicates.add(cb.equal(root.get("userId"), currentUserId));
            }

            // Search filter
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate firstName = cb.like(cb.lower(root.get("firstName")), searchPattern);
                Predicate lastName = cb.like(cb.lower(root.get("lastName")), searchPattern);
                Predicate username = cb.like(cb.lower(root.get("username")), searchPattern);
                Predicate email = cb.like(cb.lower(root.get("email")), searchPattern);
                predicates.add(cb.or(firstName, lastName, username, email));
            }

            // Department filter
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("departmentId"), departmentId));
            }

            // Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Role filter
            if (roleId != null) {
                predicates.add(cb.equal(root.get("role").get("roleId"), roleId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
