package com.hareeshvar.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hareeshvar.attendance.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByDepartmentName(String departmentName);

    boolean existsByDepartmentCode(String departmentCode);

}