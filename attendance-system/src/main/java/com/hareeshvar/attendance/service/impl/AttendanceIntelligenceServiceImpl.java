package com.hareeshvar.attendance.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hareeshvar.attendance.entity.Attendance;
import com.hareeshvar.attendance.entity.EmployeeShift;
import com.hareeshvar.attendance.entity.Leave;
import com.hareeshvar.attendance.entity.Shift;
import com.hareeshvar.attendance.enums.AttendanceStatus;
import com.hareeshvar.attendance.enums.EmployeeShiftStatus;
import com.hareeshvar.attendance.enums.LeaveStatus;
import com.hareeshvar.attendance.repository.EmployeeShiftRepository;
import com.hareeshvar.attendance.repository.LeaveRepository;
import com.hareeshvar.attendance.service.AttendanceIntelligenceService;
import com.hareeshvar.attendance.service.ShiftWindow;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceIntelligenceServiceImpl implements AttendanceIntelligenceService {

    private final EmployeeShiftRepository employeeShiftRepository;
    private final LeaveRepository leaveRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Shift resolveActiveShift(Long userId, LocalDate date) {
        if (userId == null || date == null) {
            return null;
        }
        return employeeShiftRepository
                .findFirstByUserUserIdAndStatusAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        userId, EmployeeShiftStatus.ACTIVE, date)
                .map(EmployeeShift::getShift)
                .orElse(null);
    }

    @Override
    public void processAttendanceIntelligence(Attendance attendance) {
        if (attendance == null || attendance.getUser() == null) {
            return;
        }

        Long userId = attendance.getUser().getUserId();
        LocalDate date = attendance.getAttendanceDate();

        // 1. Resolve shift if not assigned
        if (attendance.getShift() == null) {
            Shift resolvedShift = resolveActiveShift(userId, date);
            if (resolvedShift != null) {
                attendance.setShift(resolvedShift);
            }
        }

        Shift shift = attendance.getShift();
        ShiftWindow shiftWindow = ShiftWindow.resolve(shift, date);
        Set<String> exceptions = new LinkedHashSet<>();

        // 2. Check-In Lateness Math
        if (attendance.getCheckInTime() != null) {
            if (shiftWindow != null) {
                LocalDateTime checkInDt = LocalDateTime.of(date, attendance.getCheckInTime());
                LocalDateTime deadline = shiftWindow.getStartDateTime()
                        .plusMinutes(shiftWindow.getGraceMinutes());

                if (checkInDt.isAfter(deadline)) {
                    int lateMins = (int) Duration.between(shiftWindow.getStartDateTime(), checkInDt).toMinutes();
                    attendance.setLateMinutes(Math.max(0, lateMins));
                    exceptions.add("LATE_ARRIVAL");
                    if (attendance.getStatus() != AttendanceStatus.HALF_DAY) {
                        attendance.setStatus(AttendanceStatus.LATE);
                    }
                } else {
                    attendance.setLateMinutes(0);
                    if (attendance.getStatus() == null || attendance.getStatus() == AttendanceStatus.LATE) {
                        attendance.setStatus(AttendanceStatus.PRESENT);
                    }
                }
            } else {
                attendance.setLateMinutes(0);
                exceptions.add("NO_SHIFT_ASSIGNED");
                if (attendance.getStatus() == null) {
                    attendance.setStatus(AttendanceStatus.PRESENT);
                }
            }
        }

        // 3. Check-Out Duration, Early Departure & Overtime Math
        LocalTime checkInTime = attendance.getCheckInTime();
        LocalTime checkOutTime = attendance.getCheckOutTime();

        if (checkInTime != null && checkOutTime != null) {
            LocalDateTime checkInDt;
            LocalDateTime checkOutDt;

            if (shiftWindow != null && shiftWindow.isOvernight()) {
                checkInDt = LocalDateTime.of(date, checkInTime);
                checkOutDt = checkOutTime.isBefore(checkInTime)
                        ? LocalDateTime.of(date.plusDays(1), checkOutTime)
                        : LocalDateTime.of(date, checkOutTime);
            } else {
                checkInDt = LocalDateTime.of(date, checkInTime);
                checkOutDt = LocalDateTime.of(date, checkOutTime);
            }

            int workMins = Math.max(0, (int) Duration.between(checkInDt, checkOutDt).toMinutes());
            attendance.setWorkingMinutes(workMins);
            attendance.setWorkingHours(workMins / 60.0);

            if (shiftWindow != null) {
                // Early departure
                if (checkOutDt.isBefore(shiftWindow.getEndDateTime())) {
                    int earlyMins = (int) Duration.between(checkOutDt, shiftWindow.getEndDateTime()).toMinutes();
                    attendance.setEarlyDepartureMinutes(Math.max(0, earlyMins));
                    exceptions.add("EARLY_DEPARTURE");
                } else {
                    attendance.setEarlyDepartureMinutes(0);
                }

                // Overtime
                if (workMins > shiftWindow.getScheduledMinutes()) {
                    int overtimeMins = workMins - shiftWindow.getScheduledMinutes();
                    attendance.setOvertimeMinutes(Math.max(0, overtimeMins));
                    exceptions.add("OVERTIME");
                } else {
                    attendance.setOvertimeMinutes(0);
                }
            } else {
                attendance.setEarlyDepartureMinutes(0);
                attendance.setOvertimeMinutes(0);
            }
        } else if (checkInTime != null && checkOutTime == null) {
            attendance.setWorkingMinutes(0);
            attendance.setWorkingHours(null);
            attendance.setEarlyDepartureMinutes(0);
            attendance.setOvertimeMinutes(0);

            if (isMissingCheckout(attendance)) {
                exceptions.add("MISSING_CHECKOUT");
            }
        }

        // 4. Approved Leave Interaction
        if (hasApprovedLeave(userId, date)) {
            exceptions.remove("LATE_ARRIVAL");
            exceptions.remove("EARLY_DEPARTURE");
            exceptions.remove("MISSING_CHECKOUT");
            exceptions.add("ON_LEAVE");
        }

        // 5. Serialize exceptions to JSON
        try {
            attendance.setExceptionsJson(objectMapper.writeValueAsString(exceptions));
        } catch (Exception e) {
            attendance.setExceptionsJson("[]");
        }
    }

    @Override
    public boolean isMissingCheckout(Attendance attendance) {
        if (attendance == null || attendance.getCheckInTime() == null || attendance.getCheckOutTime() != null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate attendanceDate = attendance.getAttendanceDate();

        if (attendanceDate.isBefore(today)) {
            return true;
        }

        if (attendanceDate.isEqual(today)) {
            Shift shift = attendance.getShift();
            if (shift != null) {
                ShiftWindow window = ShiftWindow.resolve(shift, today);
                if (window != null && LocalDateTime.now().isAfter(window.getEndDateTime())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasApprovedLeave(Long userId, LocalDate date) {
        List<Leave> leaves = leaveRepository.findByUserUserId(userId);
        return leaves.stream().anyMatch(l ->
                l.getStatus() == LeaveStatus.APPROVED &&
                (date.isEqual(l.getStartDate()) || date.isAfter(l.getStartDate())) &&
                (date.isEqual(l.getEndDate()) || date.isBefore(l.getEndDate()))
        );
    }
}
