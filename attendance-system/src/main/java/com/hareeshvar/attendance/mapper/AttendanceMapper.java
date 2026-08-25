package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.AttendanceRequestDTO;
import com.hareeshvar.attendance.dto.response.AttendanceResponseDTO;
import com.hareeshvar.attendance.entity.Attendance;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "attendanceId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Attendance toEntity(AttendanceRequestDTO dto);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "department.departmentId", target = "departmentId")
    @Mapping(source = "department.departmentName", target = "departmentName")
    AttendanceResponseDTO toResponse(Attendance attendance);
}