package com.hareeshvar.attendance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hareeshvar.attendance.dto.request.ReportRequestDTO;
import com.hareeshvar.attendance.dto.response.ReportResponseDTO;
import com.hareeshvar.attendance.entity.Report;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "reportId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Report toEntity(ReportRequestDTO dto);

    ReportResponseDTO toResponse(Report report);
}
