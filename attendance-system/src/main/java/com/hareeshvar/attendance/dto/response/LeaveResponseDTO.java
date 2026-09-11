package com.hareeshvar.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hareeshvar.attendance.enums.LeaveStatus;
import com.hareeshvar.attendance.enums.LeaveType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveResponseDTO {

    private Long leaveId;

    private Long userId;

    private String username;

    private LeaveType leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String reason;

    private LeaveStatus status;

    private Integer totalDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}