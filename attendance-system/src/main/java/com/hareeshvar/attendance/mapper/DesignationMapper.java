package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.DesignationRequestDTO;
import com.hareeshvar.attendance.dto.response.DesignationResponseDTO;
import com.hareeshvar.attendance.entity.Designation;

@Mapper(componentModel = "spring")
public interface DesignationMapper {

    @Mapping(target = "designationId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Designation toEntity(DesignationRequestDTO dto);

    DesignationResponseDTO toResponse(Designation designation);
}