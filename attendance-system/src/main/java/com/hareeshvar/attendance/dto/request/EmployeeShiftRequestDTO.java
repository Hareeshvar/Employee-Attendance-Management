package com.hareeshvar.attendance.dto.request;

import java.time.LocalDate;

import com.hareeshvar.attendance.enums.EmployeeShiftStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeShiftRequestDTO {

    @NotNull
    private Long userId;

    @NotNull
    private Long shiftId;

    @NotNull
    private LocalDate effectiveDate;

    @NotNull
    private EmployeeShiftStatus status;

}