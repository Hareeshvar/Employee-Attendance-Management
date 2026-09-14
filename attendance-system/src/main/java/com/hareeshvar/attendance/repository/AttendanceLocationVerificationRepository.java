package com.hareeshvar.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hareeshvar.attendance.entity.AttendanceLocationVerification;
import com.hareeshvar.attendance.enums.PunchType;

public interface AttendanceLocationVerificationRepository extends JpaRepository<AttendanceLocationVerification, Long> {

    List<AttendanceLocationVerification> findByAttendanceAttendanceId(Long attendanceId);

    Optional<AttendanceLocationVerification> findByAttendanceAttendanceIdAndPunchType(Long attendanceId, PunchType punchType);

    long countByWorkplaceWorkplaceId(Long workplaceId);
}
