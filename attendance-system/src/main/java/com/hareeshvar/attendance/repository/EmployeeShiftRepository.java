package com.hareeshvar.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hareeshvar.attendance.entity.EmployeeShift;

public interface EmployeeShiftRepository
        extends JpaRepository<EmployeeShift, Long> {

    List<EmployeeShift> findByUserUserId(Long userId);

    java.util.Optional<EmployeeShift> findFirstByUserUserIdAndStatusAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
            Long userId,
            com.hareeshvar.attendance.enums.EmployeeShiftStatus status,
            java.time.LocalDate effectiveDate
    );
}