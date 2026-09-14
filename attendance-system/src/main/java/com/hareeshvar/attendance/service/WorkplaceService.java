package com.hareeshvar.attendance.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hareeshvar.attendance.dto.request.EmployeeWorkplaceAssignmentDTO;
import com.hareeshvar.attendance.dto.request.WorkplaceRequestDTO;
import com.hareeshvar.attendance.dto.response.MyWorkplaceResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.dto.response.WorkplaceResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;

public interface WorkplaceService {

    WorkplaceResponseDTO createWorkplace(WorkplaceRequestDTO request);

    WorkplaceResponseDTO updateWorkplace(Long id, WorkplaceRequestDTO request);

    WorkplaceResponseDTO setWorkplaceStatus(Long id, boolean active);

    void deleteWorkplace(Long id);

    WorkplaceResponseDTO getWorkplaceById(Long id);

    PageResponse<WorkplaceResponseDTO> getAllWorkplaces(Pageable pageable, String search, Boolean activeOnly);

    List<MyWorkplaceResponseDTO> getMyWorkplaces(CustomUserDetails userDetails);

    void assignEmployee(EmployeeWorkplaceAssignmentDTO request);

    void removeAssignment(Long assignmentId);
}
