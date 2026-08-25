package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.AttendanceRequestDTO;
import com.hareeshvar.attendance.dto.response.AttendanceResponseDTO;

public interface AttendanceService {

    AttendanceResponseDTO createAttendance(AttendanceRequestDTO request);

    List<AttendanceResponseDTO> getAllAttendance();

    AttendanceResponseDTO getAttendanceById(Long attendanceId);

    AttendanceResponseDTO updateAttendance(Long attendanceId,
                                           AttendanceRequestDTO request);

    void deleteAttendance(Long attendanceId);

    AttendanceResponseDTO checkIn(Long userId);

    AttendanceResponseDTO checkOut(Long userId);

}