package com.hareeshvar.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hareeshvar.attendance.entity.Attendance;
import com.hareeshvar.attendance.enums.AttendanceStatus;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserUserIdAndAttendanceDate(
            Long userId,
            LocalDate attendanceDate
    );

    List<Attendance> findByUserUserId(Long userId);

    List<Attendance> findByUserDepartmentDepartmentId(Long departmentId);

    long countByAttendanceDateAndStatus(LocalDate attendanceDate, AttendanceStatus status);

    long countByAttendanceDate(LocalDate attendanceDate);
}