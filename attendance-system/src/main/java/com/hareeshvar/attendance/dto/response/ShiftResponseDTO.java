package com.hareeshvar.attendance.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShiftResponseDTO {

    private Long shiftId;

    private String shiftName;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer workingHours;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}