package com.hareeshvar.attendance.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.LeaveRequestDTO;
import com.hareeshvar.attendance.dto.response.LeaveResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.entity.Leave;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.LeaveStatus;
import com.hareeshvar.attendance.enums.LeaveType;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.exception.BadRequestException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.LeaveMapper;
import com.hareeshvar.attendance.repository.LeaveRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.repository.specification.LeaveSpecification;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.AuditLogService;
import com.hareeshvar.attendance.service.LeaveService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final LeaveMapper leaveMapper;
    private final AuditLogService auditLogService;

    @Override
    public LeaveResponseDTO applyLeave(LeaveRequestDTO request, CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        Long targetUserId = request.getUserId();
        if (userDetails.getRole() != RoleName.ADMIN && userDetails.getRole() != RoleName.HR) {
            targetUserId = userDetails.getUserId();
        }

        if (targetUserId == null) {
            targetUserId = userDetails.getUserId();
        }

        if (targetUserId == null) {
            throw new BadRequestException("User ID is required to apply for leave");
        }

        final Long userIdToUse = targetUserId;
        User user = userRepository.findById(userIdToUse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userIdToUse));

        Leave leave = leaveMapper.toEntity(request);
        leave.setUser(user);
        leave.setStatus(LeaveStatus.PENDING);
        leave.calculateTotalDays();
        leave.syncLeaveTypeId();

        Leave saved = leaveRepository.save(leave);

        auditLogService.logSuccess(
                AuditAction.LEAVE_CREATED,
                "LEAVE_REQUEST",
                String.valueOf(saved.getLeaveId()),
                "Applied for leave: " + saved.getLeaveType() + " from " + saved.getStartDate() + " to " + saved.getEndDate(),
                Map.of(
                        "leaveType", saved.getLeaveType() != null ? saved.getLeaveType().name() : "UNKNOWN",
                        "startDate", saved.getStartDate() != null ? saved.getStartDate().toString() : "",
                        "endDate", saved.getEndDate() != null ? saved.getEndDate().toString() : ""
                )
        );

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
    public PageResponse<LeaveResponseDTO> getLeavesPaginated(
            Pageable pageable,
            String search,
            LeaveStatus status,
            LeaveType leaveType,
            CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Specification<Leave> spec = LeaveSpecification.filterLeaves(
                search,
                status,
                leaveType,
                userDetails.getRole(),
                userDetails.getDepartmentId(),
                userDetails.getUserId()
        );

        Page<Leave> page = leaveRepository.findAll(spec, pageable);
        Page<LeaveResponseDTO> dtoPage = page.map(leaveMapper::toResponse);
        return PageResponse.from(dtoPage);
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

        auditLogService.logSuccess(
                AuditAction.LEAVE_APPROVED,
                "LEAVE_REQUEST",
                String.valueOf(saved.getLeaveId()),
                "Approved leave request #" + saved.getLeaveId() + " for employee '" + leave.getUser().getUsername() + "'",
                Map.of("oldStatus", "PENDING", "newStatus", "APPROVED", "applicantUsername", leave.getUser().getUsername())
        );

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

        auditLogService.logSuccess(
                AuditAction.LEAVE_REJECTED,
                "LEAVE_REQUEST",
                String.valueOf(saved.getLeaveId()),
                "Rejected leave request #" + saved.getLeaveId() + " for employee '" + leave.getUser().getUsername() + "'",
                Map.of("oldStatus", "PENDING", "newStatus", "REJECTED", "applicantUsername", leave.getUser().getUsername())
        );

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

        auditLogService.logSuccess(
                AuditAction.LEAVE_CANCELLED,
                "LEAVE_REQUEST",
                String.valueOf(leaveId),
                "Cancelled leave request #" + leaveId + " for employee '" + leave.getUser().getUsername() + "'",
                Map.of("leaveId", leaveId, "applicantUsername", leave.getUser().getUsername())
        );
    }
}