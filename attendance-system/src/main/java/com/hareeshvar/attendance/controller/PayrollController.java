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

import com.hareeshvar.attendance.dto.request.PayrollRequestDTO;
import com.hareeshvar.attendance.dto.response.PayrollResponseDTO;
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
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollResponseDTO createPayroll(
            @Valid @RequestBody PayrollRequestDTO request) {

        return payrollService.createPayroll(request);
    }

    @GetMapping
    public List<PayrollResponseDTO> getAllPayrolls() {

        return payrollService.getAllPayrolls();
    }

    @GetMapping("/{id}")
    public PayrollResponseDTO getPayrollById(
            @PathVariable Long id) {

        return payrollService.getPayrollById(id);
    }

    @PutMapping("/{id}")
    public PayrollResponseDTO updatePayroll(
            @PathVariable Long id,
            @Valid @RequestBody PayrollRequestDTO request) {

        return payrollService.updatePayroll(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayroll(@PathVariable Long id) {

        payrollService.deletePayroll(id);
    }
}
