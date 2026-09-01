package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.LeaveRequestDTO;
import com.hareeshvar.attendance.dto.response.LeaveResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;

public interface LeaveService {

    LeaveResponseDTO applyLeave(LeaveRequestDTO request, CustomUserDetails userDetails);

    List<LeaveResponseDTO> getAllLeaves(CustomUserDetails userDetails);

    LeaveResponseDTO getLeaveById(Long leaveId, CustomUserDetails userDetails);

    LeaveResponseDTO approveLeave(Long leaveId, CustomUserDetails userDetails);

    LeaveResponseDTO rejectLeave(Long leaveId, CustomUserDetails userDetails);

    void deleteLeave(Long leaveId, CustomUserDetails userDetails);
}