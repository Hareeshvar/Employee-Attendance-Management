package com.hareeshvar.attendance.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DesignationResponseDTO {

    private Long designationId;

    private String designationName;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}