package com.hareeshvar.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hareeshvar.attendance.entity.Payroll;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
}
