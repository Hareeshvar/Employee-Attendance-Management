package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.DesignationRequestDTO;
import com.hareeshvar.attendance.dto.response.DesignationResponseDTO;

public interface DesignationService {

    DesignationResponseDTO createDesignation(
            DesignationRequestDTO request);

    List<DesignationResponseDTO> getAllDesignations();

    DesignationResponseDTO getDesignationById(Long id);

    DesignationResponseDTO updateDesignation(
            Long id,
            DesignationRequestDTO request);

    void deleteDesignation(Long id);

}