package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.AttendanceRequestDTO;
import com.hareeshvar.attendance.dto.response.AttendanceResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;

public interface AttendanceService {

    AttendanceResponseDTO createAttendance(AttendanceRequestDTO request);

    List<AttendanceResponseDTO> getAllAttendance(CustomUserDetails userDetails);

    AttendanceResponseDTO getAttendanceById(Long attendanceId, CustomUserDetails userDetails);

    AttendanceResponseDTO updateAttendance(Long attendanceId, AttendanceRequestDTO request);

    void deleteAttendance(Long attendanceId);

    AttendanceResponseDTO checkIn(Long userId);

    AttendanceResponseDTO checkOut(Long userId);

    AttendanceResponseDTO checkInWithAuth(Long targetUserId, CustomUserDetails userDetails);

    AttendanceResponseDTO checkOutWithAuth(Long targetUserId, CustomUserDetails userDetails);
}