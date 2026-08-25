package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.ReportRequestDTO;
import com.hareeshvar.attendance.dto.response.ReportResponseDTO;

public interface ReportService {

    ReportResponseDTO createReport(ReportRequestDTO request);

    List<ReportResponseDTO> getAllReports();

    ReportResponseDTO getReportById(Long reportId);

    ReportResponseDTO updateReport(Long reportId, ReportRequestDTO request);

    void deleteReport(Long reportId);

}
