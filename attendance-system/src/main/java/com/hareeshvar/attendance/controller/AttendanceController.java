package com.hareeshvar.attendance.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hareeshvar.attendance.dto.request.AttendanceRequestDTO;
import com.hareeshvar.attendance.dto.response.AttendanceResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.enums.AttendanceStatus;
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
    public PageResponse<AttendanceResponseDTO> getAllAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "attendanceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return attendanceService.getAttendancePaginated(pageable, search, status, departmentId, startDate, endDate, userDetails);
    }

    @GetMapping("/analytics")
    public List<AttendanceResponseDTO> getAnalyticsAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return attendanceService.getAnalyticsAttendance(startDate, endDate, userDetails);
    }

    @GetMapping("/exceptions")
    public PageResponse<AttendanceResponseDTO> getAttendanceExceptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "attendanceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return attendanceService.getAttendanceExceptionsPaginated(pageable, userDetails);
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
    public ResponseEntity<AttendanceResponseDTO> checkInSelf(
            @Valid @RequestBody com.hareeshvar.attendance.dto.request.LocationPunchRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.checkInWithAuth(userDetails.getUserId(), request, userDetails));
    }

    @PostMapping("/checkout")
    public ResponseEntity<AttendanceResponseDTO> checkOutSelf(
            @Valid @RequestBody com.hareeshvar.attendance.dto.request.LocationPunchRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.checkOutWithAuth(userDetails.getUserId(), request, userDetails));
    }

    @PostMapping("/checkin/{userId}")
    public ResponseEntity<AttendanceResponseDTO> checkIn(
            @PathVariable Long userId,
            @RequestBody(required = false) com.hareeshvar.attendance.dto.request.LocationPunchRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.checkInWithAuth(userId, request, userDetails));
    }

    @PostMapping("/checkout/{userId}")
    public ResponseEntity<AttendanceResponseDTO> checkOut(
            @PathVariable Long userId,
            @RequestBody(required = false) com.hareeshvar.attendance.dto.request.LocationPunchRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.checkOutWithAuth(userId, request, userDetails));
    }
}