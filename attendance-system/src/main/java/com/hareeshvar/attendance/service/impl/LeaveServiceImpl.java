package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.LeaveRequestDTO;
import com.hareeshvar.attendance.dto.response.LeaveResponseDTO;
import com.hareeshvar.attendance.entity.Leave;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.LeaveStatus;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.exception.BadRequestException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.LeaveMapper;
import com.hareeshvar.attendance.repository.LeaveRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.LeaveService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final LeaveMapper leaveMapper;

    @Override
    public LeaveResponseDTO applyLeave(LeaveRequestDTO request, CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Long targetUserId = request.getUserId();
        if (userDetails.getRole() != RoleName.ADMIN && userDetails.getRole() != RoleName.HR) {
            // Force user identity to authenticated principal to prevent IDOR
            targetUserId = userDetails.getUserId();
        }

        if (targetUserId == null) {
            targetUserId = userDetails.getUserId();
        }

        final Long userIdToUse = targetUserId;
        User user = userRepository.findById(userIdToUse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userIdToUse));

        Leave leave = leaveMapper.toEntity(request);
        leave.setUser(user);
        leave.setStatus(LeaveStatus.PENDING);

        Leave saved = leaveRepository.save(leave);
        return leaveMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getAllLeaves(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        List<Leave> leaves;

        if (role == RoleName.ADMIN || role == RoleName.HR) {
            leaves = leaveRepository.findAll();
        } else if (role == RoleName.MANAGER) {
            Long deptId = userDetails.getDepartmentId();
            leaves = (deptId != null) ? leaveRepository.findByUserDepartmentDepartmentId(deptId) : List.of();
        } else {
            leaves = leaveRepository.findByUserUserId(userDetails.getUserId());
        }

        return leaves.stream().map(leaveMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponseDTO getLeaveById(Long leaveId, CustomUserDetails userDetails) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.ADMIN || role == RoleName.HR) {
            return leaveMapper.toResponse(leave);
        }

        if (role == RoleName.MANAGER) {
            Long recordDeptId = leave.getUser().getDepartment() != null ? leave.getUser().getDepartment().getDepartmentId() : null;
            if (recordDeptId == null || !recordDeptId.equals(userDetails.getDepartmentId())) {
                throw new AccessDeniedException("Access denied: Leave record does not belong to your department");
            }
            return leaveMapper.toResponse(leave);
        }

        // EMPLOYEE ownership check
        if (!leave.getUser().getUserId().equals(userDetails.getUserId())) {
            throw new AccessDeniedException("Access denied: You can only view your own leave requests");
        }

        return leaveMapper.toResponse(leave);
    }

    @Override
    public LeaveResponseDTO approveLeave(Long leaveId, CustomUserDetails userDetails) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.EMPLOYEE) {
            throw new AccessDeniedException("Access denied: Employees cannot approve leaves");
        }

        if (role == RoleName.MANAGER) {
            Long recordDeptId = leave.getUser().getDepartment() != null ? leave.getUser().getDepartment().getDepartmentId() : null;
            if (recordDeptId == null || !recordDeptId.equals(userDetails.getDepartmentId())) {
                throw new AccessDeniedException("Access denied: Cannot approve leave for employee in another department");
            }
        }

        leave.setStatus(LeaveStatus.APPROVED);
        Leave saved = leaveRepository.save(leave);
        return leaveMapper.toResponse(saved);
    }

    @Override
    public LeaveResponseDTO rejectLeave(Long leaveId, CustomUserDetails userDetails) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.EMPLOYEE) {
            throw new AccessDeniedException("Access denied: Employees cannot reject leaves");
        }

        if (role == RoleName.MANAGER) {
            Long recordDeptId = leave.getUser().getDepartment() != null ? leave.getUser().getDepartment().getDepartmentId() : null;
            if (recordDeptId == null || !recordDeptId.equals(userDetails.getDepartmentId())) {
                throw new AccessDeniedException("Access denied: Cannot reject leave for employee in another department");
            }
        }

        leave.setStatus(LeaveStatus.REJECTED);
        Leave saved = leaveRepository.save(leave);
        return leaveMapper.toResponse(saved);
    }

    @Override
    public void deleteLeave(Long leaveId, CustomUserDetails userDetails) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.EMPLOYEE) {
            if (!leave.getUser().getUserId().equals(userDetails.getUserId())) {
                throw new AccessDeniedException("Access denied: You can only cancel your own leave requests");
            }
            if (leave.getStatus() != LeaveStatus.PENDING) {
                throw new BadRequestException("Only pending leave requests can be cancelled");
            }
        }

        leaveRepository.delete(leave);
    }
}