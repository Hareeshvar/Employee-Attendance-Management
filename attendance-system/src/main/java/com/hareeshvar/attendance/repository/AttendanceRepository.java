package com.hareeshvar.attendance.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hareeshvar.attendance.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserUserIdAndAttendanceDate(
            Long userId,
            LocalDate attendanceDate
    );

}