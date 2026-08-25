package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.EmployeeShiftRequestDTO;
import com.hareeshvar.attendance.dto.response.EmployeeShiftResponseDTO;
import com.hareeshvar.attendance.entity.EmployeeShift;

@Mapper(componentModel = "spring")
public interface EmployeeShiftMapper {

    @Mapping(target = "employeeShiftId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EmployeeShift toEntity(EmployeeShiftRequestDTO dto);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "shift.shiftId", target = "shiftId")
    @Mapping(source = "shift.shiftName", target = "shiftName")
    EmployeeShiftResponseDTO toResponse(EmployeeShift employeeShift);

}