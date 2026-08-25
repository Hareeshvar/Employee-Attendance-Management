package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.ShiftRequestDTO;
import com.hareeshvar.attendance.dto.response.ShiftResponseDTO;
import com.hareeshvar.attendance.entity.Shift;

@Mapper(componentModel = "spring")
public interface ShiftMapper {

    @Mapping(target = "shiftId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Shift toEntity(ShiftRequestDTO dto);

    ShiftResponseDTO toResponse(Shift shift);

}