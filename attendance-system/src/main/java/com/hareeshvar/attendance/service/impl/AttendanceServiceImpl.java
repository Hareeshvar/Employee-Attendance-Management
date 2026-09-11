package com.hareeshvar.attendance.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.AttendanceRequestDTO;
import com.hareeshvar.attendance.dto.response.AttendanceResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.entity.Attendance;
import com.hareeshvar.attendance.entity.Department;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.AttendanceStatus;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.exception.BadRequestException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.AttendanceMapper;
import com.hareeshvar.attendance.repository.AttendanceRepository;
import com.hareeshvar.attendance.repository.DepartmentRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.repository.specification.AttendanceSpecification;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.AttendanceService;
import com.hareeshvar.attendance.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AttendanceResponseDTO createAttendance(AttendanceRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        Attendance attendance = attendanceMapper.toEntity(request);
        attendance.setUser(user);
        attendance.setDepartment(department);

        Attendance savedAttendance = attendanceRepository.save(attendance);

        auditLogService.logSuccess(
                AuditAction.ATTENDANCE_CHECK_IN,
                "ATTENDANCE",
                String.valueOf(savedAttendance.getAttendanceId()),
                "Manual attendance created for user '" + user.getUsername() + "'",
                Map.of("userId", user.getUserId(), "status", savedAttendance.getStatus().name())
        );

        return attendanceMapper.toResponse(savedAttendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getAllAttendance(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        List<Attendance> list;

        if (role == RoleName.ADMIN || role == RoleName.HR) {
            list = attendanceRepository.findAll();
        } else if (role == RoleName.MANAGER) {
            Long deptId = userDetails.getDepartmentId();
            if (deptId == null) {
                list = List.of();
            } else {
                list = attendanceRepository.findByUserDepartmentDepartmentId(deptId);
            }
        } else {
            list = attendanceRepository.findByUserUserId(userDetails.getUserId());
        }

        return list.stream().map(attendanceMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponseDTO> getAttendancePaginated(
            Pageable pageable,
            String search,
            AttendanceStatus status,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Specification<Attendance> spec = AttendanceSpecification.filterAttendance(
                search,
                status,
                departmentId,
                startDate,
                endDate,
                userDetails.getRole(),
                userDetails.getDepartmentId(),
                userDetails.getUserId()
        );

        Page<Attendance> page = attendanceRepository.findAll(spec, pageable);
        Page<AttendanceResponseDTO> dtoPage = page.map(attendanceMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getAnalyticsAttendance(
            LocalDate startDate,
            LocalDate endDate,
            CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Specification<Attendance> spec = AttendanceSpecification.filterAttendance(
                null,
                null,
                null,
                startDate,
                endDate,
                userDetails.getRole(),
                userDetails.getDepartmentId(),
                userDetails.getUserId()
        );

        List<Attendance> list = attendanceRepository.findAll(spec);
        return list.stream().map(attendanceMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponseDTO getAttendanceById(Long attendanceId, CustomUserDetails userDetails) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.ADMIN || role == RoleName.HR) {
            return attendanceMapper.toResponse(attendance);
        }

        if (role == RoleName.MANAGER) {
            Long recordDeptId = attendance.getDepartment() != null ? attendance.getDepartment().getDepartmentId() : null;
            if (recordDeptId == null || !recordDeptId.equals(userDetails.getDepartmentId())) {
                throw new AccessDeniedException("Access denied: Attendance record does not belong to your department");
            }
            return attendanceMapper.toResponse(attendance);
        }

        if (!attendance.getUser().getUserId().equals(userDetails.getUserId())) {
            throw new AccessDeniedException("Access denied: You can only view your own attendance records");
        }

        return attendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional
    public AttendanceResponseDTO updateAttendance(Long attendanceId, AttendanceRequestDTO request) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        attendance.setUser(user);
        attendance.setDepartment(department);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setWorkingHours(request.getWorkingHours());
        attendance.setStatus(request.getStatus());

        Attendance updatedAttendance = attendanceRepository.save(attendance);

        auditLogService.logSuccess(
                AuditAction.ATTENDANCE_UPDATED,
                "ATTENDANCE",
                String.valueOf(updatedAttendance.getAttendanceId()),
                "Updated attendance record #" + updatedAttendance.getAttendanceId() + " for user '" + user.getUsername() + "'",
                Map.of("attendanceId", updatedAttendance.getAttendanceId(), "status", updatedAttendance.getStatus().name())
        );

        return attendanceMapper.toResponse(updatedAttendance);
    }

    @Override
    @Transactional
    public void deleteAttendance(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));
        attendanceRepository.delete(attendance);

        auditLogService.logSuccess(
                AuditAction.ATTENDANCE_DELETED,
                "ATTENDANCE",
                String.valueOf(attendanceId),
                "Deleted attendance record #" + attendanceId,
                Map.of("attendanceId", attendanceId)
        );
    }

    @Override
    @Transactional
    public AttendanceResponseDTO checkIn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

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

        auditLogService.logSuccess(
                AuditAction.ATTENDANCE_CHECK_IN,
                "ATTENDANCE",
                String.valueOf(saved.getAttendanceId()),
                "Employee '" + user.getUsername() + "' checked in",
                Map.of("userId", user.getUserId(), "checkInTime", saved.getCheckInTime().toString())
        );

        return attendanceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponseDTO checkOut(Long userId) {
        Attendance attendance = attendanceRepository
                .findByUserUserIdAndAttendanceDate(userId, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance check-in not found for today"));

        LocalTime checkOut = LocalTime.now();
        attendance.setCheckOutTime(checkOut);

        double hours = Duration.between(
                attendance.getCheckInTime(),
                checkOut
        ).toMinutes() / 60.0;

        attendance.setWorkingHours(hours);

        Attendance updated = attendanceRepository.save(attendance);

        auditLogService.logSuccess(
                AuditAction.ATTENDANCE_CHECK_OUT,
                "ATTENDANCE",
                String.valueOf(updated.getAttendanceId()),
                "Employee '" + updated.getUser().getUsername() + "' checked out",
                Map.of("userId", updated.getUser().getUserId(), "workingHours", updated.getWorkingHours())
        );

        return attendanceMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public AttendanceResponseDTO checkInWithAuth(Long targetUserId, CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Long userIdToUse = (targetUserId != null) ? targetUserId : userDetails.getUserId();

        if (userDetails.getRole() != RoleName.ADMIN && userDetails.getRole() != RoleName.HR) {
            if (!userDetails.getUserId().equals(userIdToUse)) {
                throw new AccessDeniedException("Access denied: You can only check in for yourself");
            }
        }

        return checkIn(userIdToUse);
    }

    @Override
    @Transactional
    public AttendanceResponseDTO checkOutWithAuth(Long targetUserId, CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Long userIdToUse = (targetUserId != null) ? targetUserId : userDetails.getUserId();

        if (userDetails.getRole() != RoleName.ADMIN && userDetails.getRole() != RoleName.HR) {
            if (!userDetails.getUserId().equals(userIdToUse)) {
                throw new AccessDeniedException("Access denied: You can only check out for yourself");
            }
        }

        return checkOut(userIdToUse);
    }
}