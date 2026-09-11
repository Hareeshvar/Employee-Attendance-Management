package com.hareeshvar.attendance.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hareeshvar.attendance.dto.request.DepartmentRequestDTO;
import com.hareeshvar.attendance.dto.response.DepartmentResponseDTO;
import com.hareeshvar.attendance.entity.Department;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.exception.ResourceAlreadyExistsException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.DepartmentMapper;
import com.hareeshvar.attendance.repository.DepartmentRepository;
import com.hareeshvar.attendance.service.AuditLogService;
import com.hareeshvar.attendance.service.DepartmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final AuditLogService auditLogService;

    @Override
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO request) {

        if (departmentRepository.existsByDepartmentName(request.getDepartmentName())) {
            throw new ResourceAlreadyExistsException("Department name already exists");
        }

        if (departmentRepository.existsByDepartmentCode(request.getDepartmentCode())) {
            throw new ResourceAlreadyExistsException("Department code already exists");
        }

        Department department = departmentMapper.toEntity(request);
        Department savedDepartment = departmentRepository.save(department);

        auditLogService.logSuccess(
                AuditAction.DEPARTMENT_CREATED,
                "DEPARTMENT",
                String.valueOf(savedDepartment.getDepartmentId()),
                "Created department '" + savedDepartment.getDepartmentName() + "'",
                Map.of("departmentName", savedDepartment.getDepartmentName(), "departmentCode", savedDepartment.getDepartmentCode())
        );

        return departmentMapper.toResponse(savedDepartment);
    }

    @Override
    public List<DepartmentResponseDTO> getAllDepartments() {

        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Override
    public DepartmentResponseDTO getDepartmentById(Long departmentId) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id : " + departmentId));

        return departmentMapper.toResponse(department);
    }

    @Override
    public DepartmentResponseDTO updateDepartment(Long departmentId,
                                                  DepartmentRequestDTO request) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id : " + departmentId));

        department.setDepartmentName(request.getDepartmentName());
        department.setDepartmentCode(request.getDepartmentCode());
        department.setDescription(request.getDescription());
        department.setStatus(request.getStatus());

        Department updatedDepartment = departmentRepository.save(department);

        auditLogService.logSuccess(
                AuditAction.DEPARTMENT_UPDATED,
                "DEPARTMENT",
                String.valueOf(updatedDepartment.getDepartmentId()),
                "Updated department '" + updatedDepartment.getDepartmentName() + "'",
                Map.of("departmentName", updatedDepartment.getDepartmentName())
        );

        return departmentMapper.toResponse(updatedDepartment);
    }

    @Override
    public void deleteDepartment(Long departmentId) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id : " + departmentId));

        departmentRepository.delete(department);

        auditLogService.logSuccess(
                AuditAction.DEPARTMENT_DELETED,
                "DEPARTMENT",
                String.valueOf(departmentId),
                "Deleted department '" + department.getDepartmentName() + "'",
                Map.of("departmentName", department.getDepartmentName())
        );
    }
}