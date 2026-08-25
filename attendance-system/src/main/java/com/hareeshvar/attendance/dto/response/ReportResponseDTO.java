package com.hareeshvar.attendance.dto.response;

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
public class ReportResponseDTO {

    private Long reportId;
    private String reportName;
    private String reportType;
    private LocalDateTime generatedDate;
    private String generatedBy;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
