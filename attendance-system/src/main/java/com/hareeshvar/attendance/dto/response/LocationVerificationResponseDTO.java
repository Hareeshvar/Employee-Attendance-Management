package com.hareeshvar.attendance.dto.response;

import java.time.LocalDateTime;

import com.hareeshvar.attendance.enums.PunchType;
import com.hareeshvar.attendance.enums.VerificationMethod;

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
public class LocationVerificationResponseDTO {

    private PunchType punchType;
    private Long workplaceId;
    private String workplaceName;
    private Double distanceMeters;
    private Double locationAccuracyMeters;
    private Boolean locationVerified;
    private VerificationMethod verificationMethod;
    private LocalDateTime verifiedAt;
}
