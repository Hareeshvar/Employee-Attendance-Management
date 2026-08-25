package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hareeshvar.attendance.dto.request.ReportRequestDTO;
import com.hareeshvar.attendance.dto.response.ReportResponseDTO;
import com.hareeshvar.attendance.entity.Report;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.ReportMapper;
import com.hareeshvar.attendance.repository.ReportRepository;
import com.hareeshvar.attendance.service.ReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    @Override
    public ReportResponseDTO createReport(ReportRequestDTO request) {

        Report report = reportMapper.toEntity(request);
        if (request.getGeneratedDate() != null) {
            report.setGeneratedDate(request.getGeneratedDate());
        }

        Report savedReport = reportRepository.save(report);

        return reportMapper.toResponse(savedReport);
    }

    @Override
    public List<ReportResponseDTO> getAllReports() {

        return reportRepository.findAll()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    public ReportResponseDTO getReportById(Long reportId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Report not found with id : " + reportId));

        return reportMapper.toResponse(report);
    }

    @Override
    public ReportResponseDTO updateReport(Long reportId, ReportRequestDTO request) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Report not found with id : " + reportId));

        report.setReportName(request.getReportName());
        report.setReportType(request.getReportType());
        if (request.getGeneratedDate() != null) {
            report.setGeneratedDate(request.getGeneratedDate());
        }
        report.setGeneratedBy(request.getGeneratedBy());
        report.setDescription(request.getDescription());

        Report updatedReport = reportRepository.save(report);

        return reportMapper.toResponse(updatedReport);
    }

    @Override
    public void deleteReport(Long reportId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Report not found with id : " + reportId));

        reportRepository.delete(report);
    }
}
