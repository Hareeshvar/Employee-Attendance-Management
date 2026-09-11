package com.hareeshvar.attendance.repository.specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hareeshvar.attendance.entity.AuditLog;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;

import jakarta.persistence.criteria.Predicate;

public class AuditLogSpecification {

    public static Specification<AuditLog> filterAuditLogs(
            AuditAction action,
            String entityType,
            String actorUsername,
            AuditResult result,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }

            if (StringUtils.hasText(entityType)) {
                predicates.add(cb.equal(cb.lower(root.get("entityType")), entityType.trim().toLowerCase()));
            }

            if (StringUtils.hasText(actorUsername)) {
                predicates.add(cb.equal(cb.lower(root.get("actorUsername")), actorUsername.trim().toLowerCase()));
            }

            if (result != null) {
                predicates.add(cb.equal(root.get("result"), result));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }

            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate actorPred = cb.like(cb.lower(root.get("actorUsername")), searchPattern);
                Predicate descPred = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate entityTypePred = cb.like(cb.lower(root.get("entityType")), searchPattern);
                Predicate entityIdPred = cb.like(cb.lower(root.get("entityId")), searchPattern);
                predicates.add(cb.or(actorPred, descPred, entityTypePred, entityIdPred));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
