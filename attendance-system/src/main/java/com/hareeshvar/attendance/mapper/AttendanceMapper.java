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
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "lateMinutes", ignore = true)
    @Mapping(target = "earlyDepartureMinutes", ignore = true)
    @Mapping(target = "workingMinutes", ignore = true)
    @Mapping(target = "overtimeMinutes", ignore = true)
    @Mapping(target = "exceptionsJson", ignore = true)
    @Mapping(target = "verifications", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Attendance toEntity(AttendanceRequestDTO dto);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "department.departmentId", target = "departmentId")
    @Mapping(source = "department.departmentName", target = "departmentName")
    @Mapping(source = "shift.shiftId", target = "shiftId")
    @Mapping(source = "shift.shiftName", target = "shiftName")
    @Mapping(target = "exceptions", expression = "java(parseExceptionsJson(attendance.getExceptionsJson()))")
    @Mapping(target = "checkInVerification", expression = "java(toVerificationDTO(attendance.getCheckInVerification()))")
    @Mapping(target = "checkOutVerification", expression = "java(toVerificationDTO(attendance.getCheckOutVerification()))")
    AttendanceResponseDTO toResponse(Attendance attendance);

    default com.hareeshvar.attendance.dto.response.LocationVerificationResponseDTO toVerificationDTO(com.hareeshvar.attendance.entity.AttendanceLocationVerification ver) {
        if (ver == null) {
            return null;
        }
        return com.hareeshvar.attendance.dto.response.LocationVerificationResponseDTO.builder()
                .punchType(ver.getPunchType())
                .workplaceId(ver.getWorkplace() != null ? ver.getWorkplace().getWorkplaceId() : null)
                .workplaceName(ver.getWorkplace() != null ? ver.getWorkplace().getName() : null)
                .distanceMeters(ver.getDistanceMeters())
                .locationAccuracyMeters(ver.getLocationAccuracyMeters())
                .locationVerified(ver.getLocationVerified())
                .verificationMethod(ver.getVerificationMethod())
                .verifiedAt(ver.getVerifiedAt())
                .build();
    }

    default java.util.List<String> parseExceptionsJson(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return java.util.Collections.emptyList();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}