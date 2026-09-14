package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hareeshvar.attendance.dto.request.EmployeeWorkplaceAssignmentDTO;
import com.hareeshvar.attendance.dto.request.WorkplaceRequestDTO;
import com.hareeshvar.attendance.dto.response.MyWorkplaceResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.dto.response.WorkplaceResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.WorkplaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/workplaces")
@RequiredArgsConstructor
public class WorkplaceController {

    private final WorkplaceService workplaceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public PageResponse<WorkplaceResponseDTO> getAllWorkplaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return workplaceService.getAllWorkplaces(pageable, search, activeOnly);
    }

    @GetMapping("/my-workplaces")
    public List<MyWorkplaceResponseDTO> getMyWorkplaces(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return workplaceService.getMyWorkplaces(userDetails);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public WorkplaceResponseDTO getWorkplaceById(@PathVariable Long id) {
        return workplaceService.getWorkplaceById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkplaceResponseDTO createWorkplace(@Valid @RequestBody WorkplaceRequestDTO request) {
        return workplaceService.createWorkplace(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public WorkplaceResponseDTO updateWorkplace(
            @PathVariable Long id,
            @Valid @RequestBody WorkplaceRequestDTO request) {
        return workplaceService.updateWorkplace(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public WorkplaceResponseDTO setWorkplaceStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return workplaceService.setWorkplaceStatus(id, active);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkplace(@PathVariable Long id) {
        workplaceService.deleteWorkplace(id);
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> assignEmployee(@Valid @RequestBody EmployeeWorkplaceAssignmentDTO request) {
        workplaceService.assignEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAssignment(@PathVariable Long id) {
        workplaceService.removeAssignment(id);
    }
}
