package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hareeshvar.attendance.dto.request.LeaveRequestDTO;
import com.hareeshvar.attendance.dto.response.LeaveResponseDTO;
import com.hareeshvar.attendance.entity.Leave;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.LeaveStatus;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.LeaveMapper;
import com.hareeshvar.attendance.repository.LeaveRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.service.LeaveService;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final LeaveMapper leaveMapper;

    @Override
    public LeaveResponseDTO applyLeave(LeaveRequestDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Leave leave = leaveMapper.toEntity(request);

        leave.setUser(user);
        leave.setStatus(LeaveStatus.PENDING);

        Leave saved = leaveRepository.save(leave);

        return leaveMapper.toResponse(saved);
    }

    @Override
    public List<LeaveResponseDTO> getAllLeaves() {

        return leaveRepository.findAll()
                .stream()
                .map(leaveMapper::toResponse)
                .toList();
    }

    @Override
    public LeaveResponseDTO getLeaveById(Long leaveId) {

        Leave leave = leaveRepository.findById(leaveId)
        .orElseThrow(() ->
                new ResourceNotFoundException("Leave not found"));

leave.getUser().getUsername();

return leaveMapper.toResponse(leave);
    }

    @Override
    public LeaveResponseDTO approveLeave(Long leaveId) {

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Leave not found"));

        leave.setStatus(LeaveStatus.APPROVED);

Leave saved = leaveRepository.save(leave);

// Force initialization
saved.getUser().getUsername();

return leaveMapper.toResponse(saved);
    }

    @Override
    public LeaveResponseDTO rejectLeave(Long leaveId) {

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Leave not found"));

        leave.setStatus(LeaveStatus.REJECTED);

Leave saved = leaveRepository.save(leave);

// Force initialization
saved.getUser().getUsername();

return leaveMapper.toResponse(saved);
    }

    @Override
    public void deleteLeave(Long leaveId) {

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Leave not found"));

        leaveRepository.delete(leave);
    }

}