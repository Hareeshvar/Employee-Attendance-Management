package com.hareeshvar.attendance.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.EmployeeWorkplaceAssignmentDTO;
import com.hareeshvar.attendance.dto.request.WorkplaceRequestDTO;
import com.hareeshvar.attendance.dto.response.MyWorkplaceResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.dto.response.WorkplaceResponseDTO;
import com.hareeshvar.attendance.entity.EmployeeWorkplace;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.entity.Workplace;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.exception.BadRequestException;
import com.hareeshvar.attendance.exception.BusinessRuleViolationException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.repository.AttendanceLocationVerificationRepository;
import com.hareeshvar.attendance.repository.EmployeeWorkplaceRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.repository.WorkplaceRepository;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.AuditLogService;
import com.hareeshvar.attendance.service.WorkplaceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkplaceServiceImpl implements WorkplaceService {

    private final WorkplaceRepository workplaceRepository;
    private final EmployeeWorkplaceRepository employeeWorkplaceRepository;
    private final AttendanceLocationVerificationRepository attendanceLocationVerificationRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final Clock clock;

    @Override
    @Transactional
    public WorkplaceResponseDTO createWorkplace(WorkplaceRequestDTO request) {
        if (workplaceRepository.existsByName(request.getName())) {
            throw new BadRequestException("Workplace with name '" + request.getName() + "' already exists");
        }
        if (workplaceRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Workplace with code '" + request.getCode() + "' already exists");
        }

        Workplace workplace = Workplace.builder()
                .name(request.getName().trim())
                .code(request.getCode().trim().toUpperCase())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .radiusMeters(request.getRadiusMeters() != null ? request.getRadiusMeters() : 100.0)
                .maxAccuracyMeters(request.getMaxAccuracyMeters() != null ? request.getMaxAccuracyMeters() : 100.0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .description(request.getDescription())
                .build();

        Workplace saved = workplaceRepository.save(workplace);

        auditLogService.logSuccess(
                AuditAction.WORKPLACE_CREATED,
                "WORKPLACE",
                String.valueOf(saved.getWorkplaceId()),
                "Workplace '" + saved.getName() + "' created",
                Map.of("code", saved.getCode(), "radiusMeters", saved.getRadiusMeters())
        );

        return toDTO(saved);
    }

    @Override
    @Transactional
    public WorkplaceResponseDTO updateWorkplace(Long id, WorkplaceRequestDTO request) {
        Workplace workplace = workplaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workplace", "id", id));

        if (workplaceRepository.existsByNameAndWorkplaceIdNot(request.getName().trim(), id)) {
            throw new BadRequestException("Workplace with name '" + request.getName() + "' already exists");
        }
        if (workplaceRepository.existsByCodeAndWorkplaceIdNot(request.getCode().trim().toUpperCase(), id)) {
            throw new BadRequestException("Workplace with code '" + request.getCode() + "' already exists");
        }

        workplace.setName(request.getName().trim());
        workplace.setCode(request.getCode().trim().toUpperCase());
        workplace.setLatitude(request.getLatitude());
        workplace.setLongitude(request.getLongitude());
        if (request.getRadiusMeters() != null) {
            workplace.setRadiusMeters(request.getRadiusMeters());
        }
        if (request.getMaxAccuracyMeters() != null) {
            workplace.setMaxAccuracyMeters(request.getMaxAccuracyMeters());
        }
        if (request.getIsActive() != null) {
            workplace.setIsActive(request.getIsActive());
        }
        workplace.setDescription(request.getDescription());

        Workplace updated = workplaceRepository.save(workplace);

        auditLogService.logSuccess(
                AuditAction.WORKPLACE_UPDATED,
                "WORKPLACE",
                String.valueOf(updated.getWorkplaceId()),
                "Workplace '" + updated.getName() + "' updated",
                Map.of("code", updated.getCode(), "isActive", updated.getIsActive())
        );

        return toDTO(updated);
    }

    @Override
    @Transactional
    public WorkplaceResponseDTO setWorkplaceStatus(Long id, boolean active) {
        Workplace workplace = workplaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workplace", "id", id));

        workplace.setIsActive(active);
        Workplace updated = workplaceRepository.save(workplace);

        auditLogService.logSuccess(
                active ? AuditAction.WORKPLACE_ACTIVATED : AuditAction.WORKPLACE_DEACTIVATED,
                "WORKPLACE",
                String.valueOf(updated.getWorkplaceId()),
                "Workplace '" + updated.getName() + "' status set to " + (active ? "ACTIVE" : "INACTIVE"),
                Map.of("isActive", active)
        );

        return toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteWorkplace(Long id) {
        Workplace workplace = workplaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workplace", "id", id));

        long verificationCount = attendanceLocationVerificationRepository.countByWorkplaceWorkplaceId(id);
        if (verificationCount > 0) {
            throw new BusinessRuleViolationException("Cannot delete workplace with associated historical attendance records. Deactivate the workplace instead.");
        }

        List<EmployeeWorkplace> assignments = employeeWorkplaceRepository.findByWorkplaceWorkplaceId(id);
        employeeWorkplaceRepository.deleteAll(assignments);

        workplaceRepository.delete(workplace);

        auditLogService.logSuccess(
                AuditAction.WORKPLACE_DELETED,
                "WORKPLACE",
                String.valueOf(id),
                "Workplace '" + workplace.getName() + "' deleted",
                Map.of("workplaceId", id, "code", workplace.getCode())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WorkplaceResponseDTO getWorkplaceById(Long id) {
        Workplace workplace = workplaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workplace", "id", id));
        return toDTO(workplace);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkplaceResponseDTO> getAllWorkplaces(Pageable pageable, String search, Boolean activeOnly) {
        Specification<Workplace> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (activeOnly != null && activeOnly) {
                predicates = cb.and(predicates, cb.isTrue(root.get("isActive")));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("code")), pattern)
                ));
            }
            return predicates;
        };

        Page<Workplace> page = workplaceRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyWorkplaceResponseDTO> getMyWorkplaces(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return List.of();
        }

        LocalDate today = LocalDate.now(clock);
        List<EmployeeWorkplace> assignments = employeeWorkplaceRepository.findActiveAssignmentsOnDate(userDetails.getUserId(), today);

        return assignments.stream()
                .filter(ew -> ew.getWorkplace() != null && Boolean.TRUE.equals(ew.getWorkplace().getIsActive()))
                .map(ew -> MyWorkplaceResponseDTO.builder()
                        .workplaceId(ew.getWorkplace().getWorkplaceId())
                        .name(ew.getWorkplace().getName())
                        .radiusMeters(ew.getWorkplace().getRadiusMeters())
                        .isPrimary(ew.getIsPrimary())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void assignEmployee(EmployeeWorkplaceAssignmentDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Workplace workplace = workplaceRepository.findById(request.getWorkplaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workplace", "id", request.getWorkplaceId()));

        LocalDate reqFrom = request.getEffectiveFrom();
        LocalDate reqTo = request.getEffectiveTo();

        if (reqTo != null && reqTo.isBefore(reqFrom)) {
            throw new BadRequestException("Effective to date cannot be before effective from date");
        }

        // Temporal overlap prevention rule for SAME user and SAME workplace
        List<EmployeeWorkplace> existingAssignments = employeeWorkplaceRepository
                .findByUserUserIdAndWorkplaceWorkplaceIdAndStatus(user.getUserId(), workplace.getWorkplaceId(), "ACTIVE");

        for (EmployeeWorkplace exist : existingAssignments) {
            LocalDate existFrom = exist.getEffectiveFrom();
            LocalDate existTo = exist.getEffectiveTo();

            boolean overlaps = (reqTo == null || !reqTo.isBefore(existFrom)) &&
                               (existTo == null || !existTo.isBefore(reqFrom));

            if (overlaps) {
                throw new BusinessRuleViolationException("An active assignment for this workplace already exists within the specified date range.");
            }
        }

        // If this assignment is primary, unset primary on any overlapping assignments for this user
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            List<EmployeeWorkplace> userAssignments = employeeWorkplaceRepository.findByUserUserId(user.getUserId());
            for (EmployeeWorkplace ew : userAssignments) {
                if (Boolean.TRUE.equals(ew.getIsPrimary())) {
                    ew.setIsPrimary(false);
                    employeeWorkplaceRepository.save(ew);
                }
            }
        }

        EmployeeWorkplace assignment = EmployeeWorkplace.builder()
                .user(user)
                .workplace(workplace)
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .status("ACTIVE")
                .effectiveFrom(reqFrom)
                .effectiveTo(reqTo)
                .build();

        employeeWorkplaceRepository.save(assignment);

        auditLogService.logSuccess(
                AuditAction.WORKPLACE_ASSIGNED,
                "EMPLOYEE_WORKPLACE",
                String.valueOf(user.getUserId()),
                "Assigned employee '" + user.getUsername() + "' to workplace '" + workplace.getName() + "'",
                Map.of("userId", user.getUserId(), "workplaceId", workplace.getWorkplaceId(), "isPrimary", assignment.getIsPrimary())
        );
    }

    @Override
    @Transactional
    public void removeAssignment(Long assignmentId) {
        EmployeeWorkplace assignment = employeeWorkplaceRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeWorkplace", "id", assignmentId));

        assignment.setStatus("INACTIVE");
        employeeWorkplaceRepository.save(assignment);

        auditLogService.logSuccess(
                AuditAction.WORKPLACE_DEACTIVATED,
                "EMPLOYEE_WORKPLACE",
                String.valueOf(assignmentId),
                "Deactivated workplace assignment #" + assignmentId,
                Map.of("assignmentId", assignmentId)
        );
    }

    private WorkplaceResponseDTO toDTO(Workplace workplace) {
        long count = employeeWorkplaceRepository.countByWorkplaceWorkplaceId(workplace.getWorkplaceId());
        return WorkplaceResponseDTO.builder()
                .workplaceId(workplace.getWorkplaceId())
                .name(workplace.getName())
                .code(workplace.getCode())
                .latitude(workplace.getLatitude())
                .longitude(workplace.getLongitude())
                .radiusMeters(workplace.getRadiusMeters())
                .maxAccuracyMeters(workplace.getMaxAccuracyMeters())
                .isActive(workplace.getIsActive())
                .description(workplace.getDescription())
                .assignedEmployeeCount(count)
                .createdAt(workplace.getCreatedAt())
                .updatedAt(workplace.getUpdatedAt())
                .build();
    }
}
