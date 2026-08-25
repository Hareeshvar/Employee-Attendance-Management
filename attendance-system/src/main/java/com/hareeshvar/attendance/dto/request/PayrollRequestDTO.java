package com.hareeshvar.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class PayrollRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Month is required")
    private String month;

    @NotNull(message = "Year is required")
    @Positive(message = "Year must be positive")
    private Integer year;

    @NotNull(message = "Basic salary is required")
    @Positive(message = "Basic salary must be positive")
    private Double basicSalary;

    @NotNull(message = "Bonus is required")
    private Double bonus;

    @NotNull(message = "Deduction is required")
    private Double deduction;

    @NotNull(message = "Net salary is required")
    @Positive(message = "Net salary must be positive")
    private Double netSalary;

}
