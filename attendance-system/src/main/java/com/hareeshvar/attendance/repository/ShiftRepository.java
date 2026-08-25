package com.hareeshvar.attendance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hareeshvar.attendance.entity.Shift;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    Optional<Shift> findByShiftName(String shiftName);

    boolean existsByShiftName(String shiftName);

}