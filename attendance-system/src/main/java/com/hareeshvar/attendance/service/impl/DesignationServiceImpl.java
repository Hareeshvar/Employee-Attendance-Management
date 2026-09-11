package com.hareeshvar.attendance.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.DesignationRequestDTO;
import com.hareeshvar.attendance.dto.response.DesignationResponseDTO;
import com.hareeshvar.attendance.entity.Designation;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.exception.ResourceAlreadyExistsException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.DesignationMapper;
import com.hareeshvar.attendance.repository.DesignationRepository;
import com.hareeshvar.attendance.service.AuditLogService;
import com.hareeshvar.attendance.service.DesignationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DesignationMapper designationMapper;
    private final AuditLogService auditLogService;

    @Override
    public DesignationResponseDTO createDesignation(DesignationRequestDTO request) {

        if (designationRepository.existsByDesignationName(request.getDesignationName())) {
            throw new ResourceAlreadyExistsException("Designation already exists");
        }

        Designation designation = designationMapper.toEntity(request);
        Designation saved = designationRepository.save(designation);

        auditLogService.logSuccess(
                AuditAction.DESIGNATION_CREATED,
                "DESIGNATION",
                String.valueOf(saved.getDesignationId()),
                "Created designation '" + saved.getDesignationName() + "'",
                Map.of("designationName", saved.getDesignationName())
        );

        return designationMapper.toResponse(saved);
    }

    @Override
    public List<DesignationResponseDTO> getAllDesignations() {

        return designationRepository.findAll()
                .stream()
                .map(designationMapper::toResponse)
                .toList();
    }

    @Override
    public DesignationResponseDTO getDesignationById(Long id) {

        Designation designation = designationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Designation not found"));

        return designationMapper.toResponse(designation);
    }

    @Override
    public DesignationResponseDTO updateDesignation(
            Long id,
            DesignationRequestDTO request) {

        Designation designation = designationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Designation not found"));

        designation.setDesignationName(request.getDesignationName());
        designation.setDescription(request.getDescription());

        Designation updated = designationRepository.save(designation);

        auditLogService.logSuccess(
                AuditAction.DESIGNATION_UPDATED,
                "DESIGNATION",
                String.valueOf(updated.getDesignationId()),
                "Updated designation '" + updated.getDesignationName() + "'",
                Map.of("designationName", updated.getDesignationName())
        );

        return designationMapper.toResponse(updated);
    }

    @Override
    public void deleteDesignation(Long id) {

        Designation designation = designationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Designation not found"));

        designationRepository.delete(designation);

        auditLogService.logSuccess(
                AuditAction.DESIGNATION_DELETED,
                "DESIGNATION",
                String.valueOf(id),
                "Deleted designation '" + designation.getDesignationName() + "'",
                Map.of("designationName", designation.getDesignationName())
        );
    }
}