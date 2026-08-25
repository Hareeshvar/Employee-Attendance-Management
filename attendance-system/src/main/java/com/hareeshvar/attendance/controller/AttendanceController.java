package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.hareeshvar.attendance.service.AttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponseDTO createAttendance(
            @Valid @RequestBody AttendanceRequestDTO request) {

        return attendanceService.createAttendance(request);
    }

    @GetMapping
    public List<AttendanceResponseDTO> getAllAttendance() {

        return attendanceService.getAllAttendance();
    }

    @GetMapping("/{id}")
    public AttendanceResponseDTO getAttendanceById(
            @PathVariable Long id) {

        return attendanceService.getAttendanceById(id);
    }

    @PutMapping("/{id}")
    public AttendanceResponseDTO updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequestDTO request) {

        return attendanceService.updateAttendance(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttendance(
            @PathVariable Long id) {

        attendanceService.deleteAttendance(id);
    }

    @PostMapping("/checkin/{userId}")
public ResponseEntity<AttendanceResponseDTO> checkIn(
        @PathVariable Long userId) {

    return ResponseEntity.ok(attendanceService.checkIn(userId));
}

@PostMapping("/checkout/{userId}")
public ResponseEntity<AttendanceResponseDTO> checkOut(
        @PathVariable Long userId) {

    return ResponseEntity.ok(attendanceService.checkOut(userId));
}

}