package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.EmployeeShiftRequestDTO;
import com.hareeshvar.attendance.dto.response.EmployeeShiftResponseDTO;

public interface EmployeeShiftService {

    EmployeeShiftResponseDTO assignShift(
            EmployeeShiftRequestDTO request);

    List<EmployeeShiftResponseDTO> getAllEmployeeShifts();

    EmployeeShiftResponseDTO getEmployeeShiftById(Long id);

    List<EmployeeShiftResponseDTO> getEmployeeShiftsByUser(Long userId);

    EmployeeShiftResponseDTO updateEmployeeShift(
            Long id,
            EmployeeShiftRequestDTO request);

    void deleteEmployeeShift(Long id);

}