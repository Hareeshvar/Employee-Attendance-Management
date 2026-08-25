package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.RoleRequestDTO;
import com.hareeshvar.attendance.dto.response.RoleResponseDTO;

public interface RoleService {

    RoleResponseDTO createRole(RoleRequestDTO request);

    List<RoleResponseDTO> getAllRoles();

    RoleResponseDTO getRoleById(Long roleId);

    RoleResponseDTO updateRole(Long roleId, RoleRequestDTO request);

    void deleteRole(Long roleId);
}