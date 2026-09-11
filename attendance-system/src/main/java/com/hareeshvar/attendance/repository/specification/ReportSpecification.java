package com.hareeshvar.attendance.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hareeshvar.attendance.entity.Report;
import com.hareeshvar.attendance.enums.RoleName;

import jakarta.persistence.criteria.Predicate;

public class ReportSpecification {

    public static Specification<Report> filterReports(
            String search,
            String reportType,
            RoleName role
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // RBAC report scope
            if (role == RoleName.HR) {
                predicates.add(cb.or(
                        cb.isNull(root.get("reportType")),
                        cb.not(cb.equal(cb.lower(root.get("reportType")), "system"))
                ));
            } else if (role == RoleName.MANAGER) {
                predicates.add(cb.equal(cb.lower(root.get("reportType")), "team"));
            } else if (role == RoleName.EMPLOYEE) {
                predicates.add(cb.equal(cb.lower(root.get("reportType")), "personal"));
            }

            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate name = cb.like(cb.lower(root.get("reportName")), searchPattern);
                Predicate type = cb.like(cb.lower(root.get("reportType")), searchPattern);
                Predicate desc = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate genBy = cb.like(cb.lower(root.get("generatedBy")), searchPattern);
                predicates.add(cb.or(name, type, desc, genBy));
            }

            if (StringUtils.hasText(reportType)) {
                predicates.add(cb.equal(cb.lower(root.get("reportType")), reportType.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
