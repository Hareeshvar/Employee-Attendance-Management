package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import com.hareeshvar.attendance.dto.request.PayrollRequestDTO;
import com.hareeshvar.attendance.dto.response.PayrollResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.PayrollService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
@Validated
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PAYROLL_MANAGE', 'ROLE_ADMIN', 'ROLE_HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollResponseDTO createPayroll(@Valid @RequestBody PayrollRequestDTO request) {
        return payrollService.createPayroll(request);
    }

    @GetMapping
    public List<PayrollResponseDTO> getAllPayrolls(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return payrollService.getAllPayrolls(userDetails);
    }

    @GetMapping("/{id}")
    public PayrollResponseDTO getPayrollById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return payrollService.getPayrollById(id, userDetails);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PAYROLL_MANAGE', 'ROLE_ADMIN', 'ROLE_HR')")
    public PayrollResponseDTO updatePayroll(
            @PathVariable Long id,
            @Valid @RequestBody PayrollRequestDTO request) {
        return payrollService.updatePayroll(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PAYROLL_MANAGE', 'ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayroll(@PathVariable Long id) {
        payrollService.deletePayroll(id);
    }
}
