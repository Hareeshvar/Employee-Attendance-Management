package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.LeaveRequestDTO;
import com.hareeshvar.attendance.dto.response.LeaveResponseDTO;

public interface LeaveService {

    LeaveResponseDTO applyLeave(LeaveRequestDTO request);

    List<LeaveResponseDTO> getAllLeaves();

    LeaveResponseDTO getLeaveById(Long leaveId);

    LeaveResponseDTO approveLeave(Long leaveId);

    LeaveResponseDTO rejectLeave(Long leaveId);

    void deleteLeave(Long leaveId);

}