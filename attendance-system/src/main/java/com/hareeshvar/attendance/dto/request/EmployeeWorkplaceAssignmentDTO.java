package com.hareeshvar.attendance.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWorkplaceAssignmentDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Workplace ID is required")
    private Long workplaceId;

    private Boolean isPrimary;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
