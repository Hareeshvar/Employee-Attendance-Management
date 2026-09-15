package com.hareeshvar.attendance.dto.response;

import java.time.LocalDateTime;

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
public class PayrollResponseDTO {

    private Long payrollId;
    private Long userId;
    private String username;
    private String userFirstName;
    private String userLastName;
    private String month;
    private Integer year;
    private Double basicSalary;
    private Double bonus;
    private Double deduction;
    private Double netSalary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
