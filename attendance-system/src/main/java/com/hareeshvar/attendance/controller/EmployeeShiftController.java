package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hareeshvar.attendance.dto.request.EmployeeShiftRequestDTO;
import com.hareeshvar.attendance.dto.response.EmployeeShiftResponseDTO;
import com.hareeshvar.attendance.service.EmployeeShiftService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/employee-shifts")
@RequiredArgsConstructor
public class EmployeeShiftController {

    private final EmployeeShiftService employeeShiftService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeShiftResponseDTO assignShift(
            @Valid @RequestBody EmployeeShiftRequestDTO request) {

        return employeeShiftService.assignShift(request);
    }

    @GetMapping
    public List<EmployeeShiftResponseDTO> getAllEmployeeShifts() {

        return employeeShiftService.getAllEmployeeShifts();
    }

    @GetMapping("/{id}")
    public EmployeeShiftResponseDTO getEmployeeShiftById(
            @PathVariable Long id) {

        return employeeShiftService.getEmployeeShiftById(id);
    }

    @GetMapping("/user/{userId}")
    public List<EmployeeShiftResponseDTO> getEmployeeShiftsByUser(
            @PathVariable Long userId) {

        return employeeShiftService.getEmployeeShiftsByUser(userId);
    }

    @PutMapping("/{id}")
    public EmployeeShiftResponseDTO updateEmployeeShift(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeShiftRequestDTO request) {

        return employeeShiftService.updateEmployeeShift(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployeeShift(
            @PathVariable Long id) {

        employeeShiftService.deleteEmployeeShift(id);
    }
}