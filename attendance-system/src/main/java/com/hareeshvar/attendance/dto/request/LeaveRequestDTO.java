package com.hareeshvar.attendance.dto.request;

import java.time.LocalDate;

import com.hareeshvar.attendance.enums.LeaveType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveRequestDTO {

    @NotNull
    private Long userId;

    @NotNull
    private LeaveType leaveType;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Size(max = 500)
    private String reason;

}