package com.hareeshvar.attendance.dto.request;

import com.hareeshvar.attendance.enums.RoleName;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class RoleRequestDTO {

    @NotNull(message = "Role name is required")
    private RoleName roleName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private Long departmentId;

}