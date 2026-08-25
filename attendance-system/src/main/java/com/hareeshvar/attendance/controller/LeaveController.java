package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.hareeshvar.attendance.dto.request.LeaveRequestDTO;
import com.hareeshvar.attendance.dto.response.LeaveResponseDTO;
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
            @Valid @RequestBody LeaveRequestDTO request) {

        return leaveService.applyLeave(request);
    }

    @GetMapping
    public List<LeaveResponseDTO> getAllLeaves() {

        return leaveService.getAllLeaves();
    }

    @GetMapping("/{id}")
    public LeaveResponseDTO getLeaveById(
            @PathVariable Long id) {

        return leaveService.getLeaveById(id);
    }

    @PutMapping("/{id}/approve")
    public LeaveResponseDTO approveLeave(
            @PathVariable Long id) {

        return leaveService.approveLeave(id);
    }

    @PutMapping("/{id}/reject")
    public LeaveResponseDTO rejectLeave(
            @PathVariable Long id) {

        return leaveService.rejectLeave(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeave(
            @PathVariable Long id) {

        leaveService.deleteLeave(id);
    }

}