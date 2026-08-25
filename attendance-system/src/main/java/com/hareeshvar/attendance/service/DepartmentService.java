package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.DepartmentRequestDTO;
import com.hareeshvar.attendance.dto.response.DepartmentResponseDTO;

public interface DepartmentService {

    DepartmentResponseDTO createDepartment(DepartmentRequestDTO request);

    List<DepartmentResponseDTO> getAllDepartments();

    DepartmentResponseDTO getDepartmentById(Long departmentId);

    DepartmentResponseDTO updateDepartment(Long departmentId,
                                           DepartmentRequestDTO request);

    void deleteDepartment(Long departmentId);
}