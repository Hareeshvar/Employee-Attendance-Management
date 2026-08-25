package com.hareeshvar.attendance.dto.response;

import java.time.LocalDateTime;

import com.hareeshvar.attendance.enums.DepartmentStatus;

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
public class DepartmentResponseDTO {

    private Long departmentId;

    private String departmentName;

    private String departmentCode;

    private String description;

    private DepartmentStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}