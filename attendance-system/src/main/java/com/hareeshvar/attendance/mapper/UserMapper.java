package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.UserRequestDTO;
import com.hareeshvar.attendance.dto.response.UserResponseDTO;
import com.hareeshvar.attendance.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRequestDTO dto);

    @Mapping(source = "role.roleId", target = "roleId")
    @Mapping(source = "role.roleName", target = "roleName")
    @Mapping(source = "department.departmentId", target = "departmentId")
    @Mapping(source = "department.departmentName", target = "departmentName")
    @Mapping(source = "designation.designationId", target = "designationId")
    @Mapping(source = "designation.designationName", target = "designationName")
    UserResponseDTO toResponse(User user);
}