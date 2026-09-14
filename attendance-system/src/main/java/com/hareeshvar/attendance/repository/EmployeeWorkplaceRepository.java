package com.hareeshvar.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hareeshvar.attendance.entity.EmployeeWorkplace;

public interface EmployeeWorkplaceRepository extends JpaRepository<EmployeeWorkplace, Long>, JpaSpecificationExecutor<EmployeeWorkplace> {

    List<EmployeeWorkplace> findByUserUserId(Long userId);

    List<EmployeeWorkplace> findByWorkplaceWorkplaceId(Long workplaceId);

    long countByWorkplaceWorkplaceId(Long workplaceId);

    List<EmployeeWorkplace> findByUserUserIdAndWorkplaceWorkplaceIdAndStatus(Long userId, Long workplaceId, String status);

    @Query("SELECT ew FROM EmployeeWorkplace ew WHERE ew.user.userId = :userId AND ew.status = 'ACTIVE' " +
           "AND ew.effectiveFrom <= :date AND (ew.effectiveTo IS NULL OR ew.effectiveTo >= :date) " +
           "AND ew.workplace.isActive = true")
    List<EmployeeWorkplace> findActiveAssignmentsOnDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
