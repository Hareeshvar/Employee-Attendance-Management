package com.hareeshvar.attendance.service;

import java.time.LocalDate;

import com.hareeshvar.attendance.entity.Attendance;
import com.hareeshvar.attendance.entity.Shift;

public interface AttendanceIntelligenceService {

    /**
     * Resolves active shift for employee on given date.
     * Returns null if no active shift assignment exists.
     */
    Shift resolveActiveShift(Long userId, LocalDate date);

    /**
     * Recalculates all attendance intelligence metrics (lateMinutes, earlyDepartureMinutes,
     * workingMinutes, overtimeMinutes, exceptionsJson, status) for a given attendance entity.
     */
    void processAttendanceIntelligence(Attendance attendance);

    /**
     * Identifies missing checkout for past/passed attendance records.
     * Read-only classification.
     */
    boolean isMissingCheckout(Attendance attendance);
}
