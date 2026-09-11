package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.ReportRequestDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.dto.response.ReportResponseDTO;
import com.hareeshvar.attendance.entity.Report;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.ReportMapper;
import com.hareeshvar.attendance.repository.ReportRepository;
import com.hareeshvar.attendance.repository.specification.ReportSpecification;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.ReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
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
    @Transactional(readOnly = true)
    public List<ReportResponseDTO> getAllReports(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        List<Report> reports = reportRepository.findAll();

        if (role == RoleName.ADMIN) {
            return reports.stream().map(reportMapper::toResponse).toList();
        }

        if (role == RoleName.HR) {
            return reports.stream()
                    .filter(r -> r.getReportType() == null || !r.getReportType().equalsIgnoreCase("SYSTEM"))
                    .map(reportMapper::toResponse)
                    .toList();
        }

        if (role == RoleName.MANAGER) {
            return reports.stream()
                    .filter(r -> r.getReportType() != null && r.getReportType().equalsIgnoreCase("TEAM"))
                    .map(reportMapper::toResponse)
                    .toList();
        }

        // EMPLOYEE scope
        return reports.stream()
                .filter(r -> r.getReportType() != null && r.getReportType().equalsIgnoreCase("PERSONAL"))
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportResponseDTO> getReportsPaginated(
            Pageable pageable,
            String search,
            String reportType,
            CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Specification<Report> spec = ReportSpecification.filterReports(
                search,
                reportType,
                userDetails.getRole()
        );

        Page<Report> page = reportRepository.findAll(spec, pageable);
        Page<ReportResponseDTO> dtoPage = page.map(reportMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getReportById(Long reportId, CustomUserDetails userDetails) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        return reportMapper.toResponse(report);
    }

    @Override
    public ReportResponseDTO updateReport(Long reportId, ReportRequestDTO request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
        reportRepository.delete(report);
    }
}
