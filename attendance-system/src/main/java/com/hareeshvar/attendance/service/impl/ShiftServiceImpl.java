package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.ShiftRequestDTO;
import com.hareeshvar.attendance.dto.response.ShiftResponseDTO;
import com.hareeshvar.attendance.entity.Shift;
import com.hareeshvar.attendance.exception.ResourceAlreadyExistsException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.ShiftMapper;
import com.hareeshvar.attendance.repository.ShiftRepository;
import com.hareeshvar.attendance.service.ShiftService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;

    @Override
    public ShiftResponseDTO createShift(ShiftRequestDTO request) {

        if (shiftRepository.existsByShiftName(request.getShiftName())) {
            throw new ResourceAlreadyExistsException("Shift already exists");
        }

        Shift shift = shiftMapper.toEntity(request);

        return shiftMapper.toResponse(
                shiftRepository.save(shift));
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
        shift.setDescription(request.getDescription());

        return shiftMapper.toResponse(
                shiftRepository.save(shift));
    }

    @Override
    public void deleteShift(Long id) {

        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shift not found"));

        shiftRepository.delete(shift);
    }
}