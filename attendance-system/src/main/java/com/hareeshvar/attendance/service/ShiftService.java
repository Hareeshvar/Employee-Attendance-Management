package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.ShiftRequestDTO;
import com.hareeshvar.attendance.dto.response.ShiftResponseDTO;

public interface ShiftService {

    ShiftResponseDTO createShift(
            ShiftRequestDTO request);

    List<ShiftResponseDTO> getAllShifts();

    ShiftResponseDTO getShiftById(Long id);

    ShiftResponseDTO updateShift(
            Long id,
            ShiftRequestDTO request);

    void deleteShift(Long id);

}