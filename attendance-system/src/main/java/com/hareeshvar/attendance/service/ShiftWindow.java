package com.hareeshvar.attendance.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.hareeshvar.attendance.entity.Shift;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShiftWindow {

    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final Integer graceMinutes;
    private final Integer scheduledMinutes;
    private final boolean isOvernight;

    public static ShiftWindow resolve(Shift shift, LocalDate attendanceDate) {
        if (shift == null || attendanceDate == null) {
            return null;
        }

        LocalTime start = shift.getStartTime();
        LocalTime end = shift.getEndTime();
        int grace = (shift.getGraceMinutes() != null) ? shift.getGraceMinutes() : 15;

        boolean overnight = end.isBefore(start) || end.equals(start);
        LocalDateTime startDt = LocalDateTime.of(attendanceDate, start);
        LocalDateTime endDt = overnight
                ? LocalDateTime.of(attendanceDate.plusDays(1), end)
                : LocalDateTime.of(attendanceDate, end);

        int scheduledMins = Math.max(0, (int) Duration.between(startDt, endDt).toMinutes());

        return new ShiftWindow(startDt, endDt, grace, scheduledMins, overnight);
    }
}
