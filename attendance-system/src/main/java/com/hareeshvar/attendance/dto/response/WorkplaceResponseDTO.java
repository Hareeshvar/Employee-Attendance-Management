package com.hareeshvar.attendance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceResponseDTO {

    private Long workplaceId;
    private String name;
    private String code;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double radiusMeters;
    private Double maxAccuracyMeters;
    private Boolean isActive;
    private String description;
    private Long assignedEmployeeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
