package com.hareeshvar.attendance.service;

import java.util.List;
import org.springframework.data.domain.Pageable;

import com.hareeshvar.attendance.dto.request.ReportRequestDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.dto.response.ReportResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;

public interface ReportService {

    ReportResponseDTO createReport(ReportRequestDTO request);

    List<ReportResponseDTO> getAllReports(CustomUserDetails userDetails);

    PageResponse<ReportResponseDTO> getReportsPaginated(
            Pageable pageable,
            String search,
            String reportType,
            CustomUserDetails userDetails
    );

    ReportResponseDTO getReportById(Long reportId, CustomUserDetails userDetails);

    ReportResponseDTO updateReport(Long reportId, ReportRequestDTO request);

    void deleteReport(Long reportId);
}
