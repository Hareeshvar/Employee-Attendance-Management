package com.hareeshvar.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.hareeshvar.attendance.entity.Leave;
import com.hareeshvar.attendance.enums.LeaveStatus;

public interface LeaveRepository extends JpaRepository<Leave, Long>, JpaSpecificationExecutor<Leave> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Leave> findAll();

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Leave> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<Leave> findByUserUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Leave> findByUserDepartmentDepartmentId(Long departmentId);

    long countByStatus(LeaveStatus status);
}