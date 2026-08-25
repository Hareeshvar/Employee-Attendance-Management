package com.hareeshvar.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hareeshvar.attendance.enums.EmployeeShiftStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeShiftResponseDTO {

    private Long employeeShiftId;

    private Long userId;

    private String username;

    private Long shiftId;

    private String shiftName;

    private LocalDate effectiveDate;

    private EmployeeShiftStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}