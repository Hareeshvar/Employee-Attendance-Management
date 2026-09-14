package com.hareeshvar.attendance.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hareeshvar.attendance.dto.request.AttendanceRequestDTO;
import com.hareeshvar.attendance.dto.response.AttendanceResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.enums.AttendanceStatus;
import com.hareeshvar.attendance.security.service.CustomUserDetails;

public interface AttendanceService {

    AttendanceResponseDTO createAttendance(AttendanceRequestDTO request);

    List<AttendanceResponseDTO> getAllAttendance(CustomUserDetails userDetails);

    PageResponse<AttendanceResponseDTO> getAttendancePaginated(
            Pageable pageable,
            String search,
            AttendanceStatus status,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            CustomUserDetails userDetails
    );

    List<AttendanceResponseDTO> getAnalyticsAttendance(
            LocalDate startDate,
            LocalDate endDate,
            CustomUserDetails userDetails
    );

    AttendanceResponseDTO getAttendanceById(Long attendanceId, CustomUserDetails userDetails);

    AttendanceResponseDTO updateAttendance(Long attendanceId, AttendanceRequestDTO request);

    void deleteAttendance(Long attendanceId);

    AttendanceResponseDTO checkIn(Long userId);

    AttendanceResponseDTO checkOut(Long userId);

    AttendanceResponseDTO checkInWithAuth(Long targetUserId, com.hareeshvar.attendance.dto.request.LocationPunchRequestDTO locationRequest, CustomUserDetails userDetails);

    AttendanceResponseDTO checkOutWithAuth(Long targetUserId, com.hareeshvar.attendance.dto.request.LocationPunchRequestDTO locationRequest, CustomUserDetails userDetails);

    PageResponse<AttendanceResponseDTO> getAttendanceExceptionsPaginated(
            Pageable pageable,
            CustomUserDetails userDetails
    );
}