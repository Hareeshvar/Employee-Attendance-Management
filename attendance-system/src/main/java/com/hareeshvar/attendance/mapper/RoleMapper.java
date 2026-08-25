package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.RoleRequestDTO;
import com.hareeshvar.attendance.dto.response.RoleResponseDTO;
import com.hareeshvar.attendance.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "roleId", ignore = true)
    Role toEntity(RoleRequestDTO dto);

    RoleResponseDTO toResponse(Role entity);

}