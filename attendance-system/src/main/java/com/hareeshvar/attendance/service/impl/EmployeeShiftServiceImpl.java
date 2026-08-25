package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.EmployeeShiftRequestDTO;
import com.hareeshvar.attendance.dto.response.EmployeeShiftResponseDTO;
import com.hareeshvar.attendance.entity.EmployeeShift;
import com.hareeshvar.attendance.entity.Shift;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.EmployeeShiftMapper;
import com.hareeshvar.attendance.repository.EmployeeShiftRepository;
import com.hareeshvar.attendance.repository.ShiftRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.service.EmployeeShiftService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeShiftServiceImpl implements EmployeeShiftService {

    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeShiftMapper employeeShiftMapper;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;

    @Override
    public EmployeeShiftResponseDTO assignShift(EmployeeShiftRequestDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shift not found"));

        EmployeeShift employeeShift = employeeShiftMapper.toEntity(request);

        employeeShift.setUser(user);
        employeeShift.setShift(shift);

        EmployeeShift saved = employeeShiftRepository.save(employeeShift);

        return employeeShiftMapper.toResponse(saved);
    }

    @Override
    public List<EmployeeShiftResponseDTO> getAllEmployeeShifts() {

        return employeeShiftRepository.findAll()
                .stream()
                .map(employeeShiftMapper::toResponse)
                .toList();
    }

    @Override
    public EmployeeShiftResponseDTO getEmployeeShiftById(Long id) {

        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee Shift not found"));

        return employeeShiftMapper.toResponse(employeeShift);
    }

    @Override
    public List<EmployeeShiftResponseDTO> getEmployeeShiftsByUser(Long userId) {

        return employeeShiftRepository.findByUserUserId(userId)
                .stream()
                .map(employeeShiftMapper::toResponse)
                .toList();
    }

    @Override
    public EmployeeShiftResponseDTO updateEmployeeShift(
            Long id,
            EmployeeShiftRequestDTO request) {

        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee Shift not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shift not found"));

        employeeShift.setUser(user);
        employeeShift.setShift(shift);
        employeeShift.setEffectiveDate(request.getEffectiveDate());
        employeeShift.setStatus(request.getStatus());

        return employeeShiftMapper.toResponse(
                employeeShiftRepository.save(employeeShift));
    }

    @Override
    public void deleteEmployeeShift(Long id) {

        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee Shift not found"));

        employeeShiftRepository.delete(employeeShift);
    }
}