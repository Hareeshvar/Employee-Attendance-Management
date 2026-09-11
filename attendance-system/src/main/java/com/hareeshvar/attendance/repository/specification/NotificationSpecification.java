package com.hareeshvar.attendance.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hareeshvar.attendance.entity.Notification;
import com.hareeshvar.attendance.enums.RoleName;

import jakarta.persistence.criteria.Predicate;

public class NotificationSpecification {

    public static Specification<Notification> filterNotifications(
            String search,
            Boolean isRead,
            RoleName currentUserRole,
            Long currentUserId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Scoping: non-ADMIN/HR users see notifications assigned to them
            if (currentUserRole != RoleName.ADMIN && currentUserRole != RoleName.HR) {
                predicates.add(cb.equal(root.get("user").get("userId"), currentUserId));
            }

            // Search filter
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate title = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate message = cb.like(cb.lower(root.get("message")), searchPattern);
                predicates.add(cb.or(title, message));
            }

            // isRead filter
            if (isRead != null) {
                predicates.add(cb.equal(root.get("isRead"), isRead));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
