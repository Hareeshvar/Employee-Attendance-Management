package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hareeshvar.attendance.dto.request.ReportRequestDTO;
import com.hareeshvar.attendance.dto.response.ReportResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('REPORT_READ_ALL', 'ROLE_ADMIN', 'ROLE_HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponseDTO createReport(@Valid @RequestBody ReportRequestDTO request) {
        return reportService.createReport(request);
    }

    @GetMapping
    public List<ReportResponseDTO> getAllReports(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return reportService.getAllReports(userDetails);
    }

    @GetMapping("/{id}")
    public ReportResponseDTO getReportById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return reportService.getReportById(id, userDetails);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('REPORT_READ_ALL', 'ROLE_ADMIN', 'ROLE_HR')")
    public ReportResponseDTO updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportRequestDTO request) {
        return reportService.updateReport(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
    }
}
