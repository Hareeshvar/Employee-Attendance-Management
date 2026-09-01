package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.hareeshvar.attendance.dto.request.AttendanceRequestDTO;
import com.hareeshvar.attendance.dto.response.AttendanceResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.AttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ATTENDANCE_MANAGE', 'ROLE_ADMIN', 'ROLE_HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponseDTO createAttendance(@Valid @RequestBody AttendanceRequestDTO request) {
        return attendanceService.createAttendance(request);
    }

    @GetMapping
    public List<AttendanceResponseDTO> getAllAttendance(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return attendanceService.getAllAttendance(userDetails);
    }

    @GetMapping("/{id}")
    public AttendanceResponseDTO getAttendanceById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return attendanceService.getAttendanceById(id, userDetails);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ATTENDANCE_MANAGE', 'ROLE_ADMIN', 'ROLE_HR')")
    public AttendanceResponseDTO updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequestDTO request) {
        return attendanceService.updateAttendance(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ATTENDANCE_MANAGE', 'ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
    }

    @PostMapping("/checkin")
    public ResponseEntity<AttendanceResponseDTO> checkInSelf(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.checkInWithAuth(userDetails.getUserId(), userDetails));
    }

    @PostMapping("/checkout")
    public ResponseEntity<AttendanceResponseDTO> checkOutSelf(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.checkOutWithAuth(userDetails.getUserId(), userDetails));
    }

    @PostMapping("/checkin/{userId}")
    public ResponseEntity<AttendanceResponseDTO> checkIn(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.checkInWithAuth(userId, userDetails));
    }

    @PostMapping("/checkout/{userId}")
    public ResponseEntity<AttendanceResponseDTO> checkOut(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.checkOutWithAuth(userId, userDetails));
    }
}