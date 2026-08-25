package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import com.hareeshvar.attendance.enums.AttendanceStatus;
import com.hareeshvar.attendance.exception.BadRequestException;

import com.hareeshvar.attendance.dto.request.AttendanceRequestDTO;
import com.hareeshvar.attendance.dto.response.AttendanceResponseDTO;
import com.hareeshvar.attendance.entity.Attendance;
import com.hareeshvar.attendance.entity.Department;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.AttendanceMapper;
import com.hareeshvar.attendance.repository.AttendanceRepository;
import com.hareeshvar.attendance.repository.DepartmentRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public AttendanceResponseDTO createAttendance(AttendanceRequestDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found"));

        Attendance attendance = attendanceMapper.toEntity(request);

        attendance.setUser(user);
        attendance.setDepartment(department);

        Attendance savedAttendance = attendanceRepository.save(attendance);

        return attendanceMapper.toResponse(savedAttendance);
    }

    @Override
    public List<AttendanceResponseDTO> getAllAttendance() {

        return attendanceRepository.findAll()
                .stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    @Override
    public AttendanceResponseDTO getAttendanceById(Long attendanceId) {

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Attendance not found"));

        return attendanceMapper.toResponse(attendance);
    }

    @Override
    public AttendanceResponseDTO updateAttendance(Long attendanceId,
                                                  AttendanceRequestDTO request) {

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Attendance not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found"));

        attendance.setUser(user);
        attendance.setDepartment(department);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setWorkingHours(request.getWorkingHours());
        attendance.setStatus(request.getStatus());

        Attendance updatedAttendance = attendanceRepository.save(attendance);

        return attendanceMapper.toResponse(updatedAttendance);
    }

    @Override
    public void deleteAttendance(Long attendanceId) {

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Attendance not found"));

        attendanceRepository.delete(attendance);
    }

    @Override
public AttendanceResponseDTO checkIn(Long userId) {

    User user = userRepository.findById(userId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

    LocalDate today = LocalDate.now();

    attendanceRepository.findByUserUserIdAndAttendanceDate(userId, today)
            .ifPresent(a -> {
                throw new BadRequestException("User already checked in today");
            });

    Attendance attendance = new Attendance();

    attendance.setUser(user);
    attendance.setDepartment(user.getDepartment());

    attendance.setAttendanceDate(today);

    attendance.setCheckInTime(LocalTime.now());

    attendance.setStatus(AttendanceStatus.PRESENT);

    Attendance saved = attendanceRepository.save(attendance);

    return attendanceMapper.toResponse(saved);
}

@Override
public AttendanceResponseDTO checkOut(Long userId) {

    Attendance attendance = attendanceRepository
            .findByUserUserIdAndAttendanceDate(userId, LocalDate.now())
            .orElseThrow(() ->
                    new ResourceNotFoundException("Attendance not found"));

    LocalTime checkOut = LocalTime.now();

    attendance.setCheckOutTime(checkOut);

    double hours = Duration.between(
            attendance.getCheckInTime(),
            checkOut
    ).toMinutes() / 60.0;

    attendance.setWorkingHours(hours);

    Attendance updated = attendanceRepository.save(attendance);

    return attendanceMapper.toResponse(updated);
}
}