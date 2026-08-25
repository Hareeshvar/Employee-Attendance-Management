package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
import com.hareeshvar.attendance.service.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponseDTO createReport(
            @Valid @RequestBody ReportRequestDTO request) {

        return reportService.createReport(request);
    }

    @GetMapping
    public List<ReportResponseDTO> getAllReports() {

        return reportService.getAllReports();
    }

    @GetMapping("/{id}")
    public ReportResponseDTO getReportById(
            @PathVariable Long id) {

        return reportService.getReportById(id);
    }

    @PutMapping("/{id}")
    public ReportResponseDTO updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportRequestDTO request) {

        return reportService.updateReport(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable Long id) {

        reportService.deleteReport(id);
    }
}
