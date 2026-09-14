package com.hareeshvar.attendance.dto.response;

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
public class MyWorkplaceResponseDTO {

    private Long workplaceId;
    private String name;
    private Double radiusMeters;
    private Boolean isPrimary;
}
