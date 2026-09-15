package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.PayrollRequestDTO;
import com.hareeshvar.attendance.dto.response.PayrollResponseDTO;
import com.hareeshvar.attendance.entity.Payroll;

@Mapper(componentModel = "spring")
public interface PayrollMapper {

    @Mapping(target = "payrollId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payroll toEntity(PayrollRequestDTO dto);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "userFirstName")
    @Mapping(source = "user.lastName", target = "userLastName")
    PayrollResponseDTO toResponse(Payroll payroll);
}
