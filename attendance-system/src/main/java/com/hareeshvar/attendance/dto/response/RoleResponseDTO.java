package com.hareeshvar.attendance.dto.response;

import com.hareeshvar.attendance.enums.RoleName;

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
public class RoleResponseDTO {

    private Long roleId;

    private RoleName roleName;

    private String description;

}