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

import com.hareeshvar.attendance.dto.request.ShiftRequestDTO;
import com.hareeshvar.attendance.dto.response.ShiftResponseDTO;
import com.hareeshvar.attendance.service.ShiftService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponseDTO createShift(
            @Valid @RequestBody ShiftRequestDTO request) {

        return shiftService.createShift(request);
    }

    @GetMapping
    public List<ShiftResponseDTO> getAllShifts() {

        return shiftService.getAllShifts();
    }

    @GetMapping("/{id}")
    public ShiftResponseDTO getShiftById(
            @PathVariable Long id) {

        return shiftService.getShiftById(id);
    }

    @PutMapping("/{id}")
    public ShiftResponseDTO updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequestDTO request) {

        return shiftService.updateShift(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShift(
            @PathVariable Long id) {

        shiftService.deleteShift(id);
    }
}