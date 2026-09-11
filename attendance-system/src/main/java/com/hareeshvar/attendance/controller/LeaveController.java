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

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import com.hareeshvar.attendance.dto.request.LeaveRequestDTO;
import com.hareeshvar.attendance.dto.response.LeaveResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.enums.LeaveStatus;
import com.hareeshvar.attendance.enums.LeaveType;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.LeaveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveResponseDTO applyLeave(
            @Valid @RequestBody LeaveRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return leaveService.applyLeave(request, userDetails);
    }

    @GetMapping
    public PageResponse<LeaveResponseDTO> getAllLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "leaveId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) LeaveType leaveType,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return leaveService.getLeavesPaginated(pageable, search, status, leaveType, userDetails);
    }

    @GetMapping("/{id}")
    public LeaveResponseDTO getLeaveById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return leaveService.getLeaveById(id, userDetails);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('LEAVE_APPROVE', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER')")
    public LeaveResponseDTO approveLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return leaveService.approveLeave(id, userDetails);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('LEAVE_APPROVE', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER')")
    public LeaveResponseDTO rejectLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return leaveService.rejectLeave(id, userDetails);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        leaveService.deleteLeave(id, userDetails);
    }
}