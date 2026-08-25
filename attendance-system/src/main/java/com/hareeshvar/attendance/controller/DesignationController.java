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

import com.hareeshvar.attendance.dto.request.DesignationRequestDTO;
import com.hareeshvar.attendance.dto.response.DesignationResponseDTO;
import com.hareeshvar.attendance.service.DesignationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DesignationResponseDTO createDesignation(
            @Valid @RequestBody DesignationRequestDTO request) {

        return designationService.createDesignation(request);
    }

    @GetMapping
    public List<DesignationResponseDTO> getAllDesignations() {

        return designationService.getAllDesignations();
    }

    @GetMapping("/{id}")
    public DesignationResponseDTO getDesignationById(
            @PathVariable Long id) {

        return designationService.getDesignationById(id);
    }

    @PutMapping("/{id}")
    public DesignationResponseDTO updateDesignation(
            @PathVariable Long id,
            @Valid @RequestBody DesignationRequestDTO request) {

        return designationService.updateDesignation(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDesignation(
            @PathVariable Long id) {

        designationService.deleteDesignation(id);
    }
}