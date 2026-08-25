package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.LeaveRequestDTO;
import com.hareeshvar.attendance.dto.response.LeaveResponseDTO;
import com.hareeshvar.attendance.entity.Leave;

@Mapper(componentModel = "spring")
public interface LeaveMapper {

    @Mapping(target = "leaveId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Leave toEntity(LeaveRequestDTO dto);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    LeaveResponseDTO toResponse(Leave leave);

}