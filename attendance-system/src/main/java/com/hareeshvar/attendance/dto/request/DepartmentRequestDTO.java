package com.hareeshvar.attendance.dto.request;

import com.hareeshvar.attendance.enums.DepartmentStatus;

import jakarta.validation.constraints.NotBlank;
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
public class DepartmentRequestDTO {

    @NotBlank
    private String departmentName;

    @NotBlank
    private String departmentCode;

    private String description;

    private DepartmentStatus status;

}