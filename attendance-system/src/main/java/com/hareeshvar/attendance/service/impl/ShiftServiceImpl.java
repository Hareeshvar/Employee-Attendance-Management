package com.hareeshvar.attendance.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.ShiftRequestDTO;
import com.hareeshvar.attendance.dto.response.ShiftResponseDTO;
import com.hareeshvar.attendance.entity.Shift;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.exception.ResourceAlreadyExistsException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.ShiftMapper;
import com.hareeshvar.attendance.repository.ShiftRepository;
import com.hareeshvar.attendance.service.AuditLogService;
import com.hareeshvar.attendance.service.ShiftService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;
    private final AuditLogService auditLogService;

    @Override
    public ShiftResponseDTO createShift(ShiftRequestDTO request) {

        if (shiftRepository.existsByShiftName(request.getShiftName())) {
            throw new ResourceAlreadyExistsException("Shift already exists");
        }

        Shift shift = shiftMapper.toEntity(request);
        if (shift.getGraceMinutes() == null) {
            shift.setGraceMinutes(request.getGraceMinutes() != null ? request.getGraceMinutes() : 15);
        }
        if (shift.getIsActive() == null) {
            shift.setIsActive(true);
        }
        Shift saved = shiftRepository.save(shift);

        auditLogService.logSuccess(
                AuditAction.SHIFT_CREATED,
                "SHIFT",
                String.valueOf(saved.getShiftId()),
                "Created shift '" + saved.getShiftName() + "'",
                Map.of("shiftName", saved.getShiftName())
        );

        return shiftMapper.toResponse(saved);
    }

    @Override
    public List<ShiftResponseDTO> getAllShifts() {

        return shiftRepository.findAll()
                .stream()
                .map(shiftMapper::toResponse)
                .toList();
    }

    @Override
    public ShiftResponseDTO getShiftById(Long id) {

        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shift not found"));

        return shiftMapper.toResponse(shift);
    }

    @Override
    public ShiftResponseDTO updateShift(
            Long id,
            ShiftRequestDTO request) {

        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shift not found"));

        shift.setShiftName(request.getShiftName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setWorkingHours(request.getWorkingHours());
        if (request.getGraceMinutes() != null) {
            shift.setGraceMinutes(request.getGraceMinutes());
        }
        shift.setDescription(request.getDescription());

        Shift updated = shiftRepository.save(shift);

        auditLogService.logSuccess(
                AuditAction.SHIFT_UPDATED,
                "SHIFT",
                String.valueOf(updated.getShiftId()),
                "Updated shift '" + updated.getShiftName() + "'",
                Map.of("shiftName", updated.getShiftName())
        );

        return shiftMapper.toResponse(updated);
    }

    @Override
    public void deleteShift(Long id) {

        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shift not found"));

        shiftRepository.delete(shift);

        auditLogService.logSuccess(
                AuditAction.SHIFT_DELETED,
                "SHIFT",
                String.valueOf(id),
                "Deleted shift '" + shift.getShiftName() + "'",
                Map.of("shiftName", shift.getShiftName())
        );
    }
}