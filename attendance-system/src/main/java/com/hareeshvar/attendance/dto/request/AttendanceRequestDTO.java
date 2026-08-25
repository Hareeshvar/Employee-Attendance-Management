package com.hareeshvar.attendance.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.hareeshvar.attendance.enums.AttendanceStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceRequestDTO {

    private Long userId;

    private Long departmentId;

    private LocalDate attendanceDate;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private Double workingHours;

    private AttendanceStatus status;
}