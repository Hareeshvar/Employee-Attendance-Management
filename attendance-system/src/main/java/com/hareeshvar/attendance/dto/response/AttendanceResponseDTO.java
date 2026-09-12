package com.hareeshvar.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.hareeshvar.attendance.enums.AttendanceStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceResponseDTO {

    private Long attendanceId;

    private Long userId;

    private String username;

    private Long departmentId;

    private String departmentName;

    private LocalDate attendanceDate;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private Double workingHours;

    private Long shiftId;

    private String shiftName;

    private Integer lateMinutes;

    private Integer earlyDepartureMinutes;

    private Integer workingMinutes;

    private Integer overtimeMinutes;

    private java.util.List<String> exceptions;

    private AttendanceStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}