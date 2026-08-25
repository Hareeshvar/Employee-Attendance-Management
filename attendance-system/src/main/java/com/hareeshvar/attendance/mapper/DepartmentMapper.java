package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.DepartmentRequestDTO;
import com.hareeshvar.attendance.dto.response.DepartmentResponseDTO;
import com.hareeshvar.attendance.entity.Department;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Department toEntity(DepartmentRequestDTO dto);

    DepartmentResponseDTO toResponse(Department department);

}