package com.hareeshvar.attendance.service;

import java.util.List;
import org.springframework.data.domain.Pageable;

import com.hareeshvar.attendance.dto.request.PayrollRequestDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.dto.response.PayrollResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;

public interface PayrollService {

    PayrollResponseDTO createPayroll(PayrollRequestDTO request);

    List<PayrollResponseDTO> getAllPayrolls(CustomUserDetails userDetails);

    PageResponse<PayrollResponseDTO> getPayrollsPaginated(
            Pageable pageable,
            String search,
            String month,
            Integer year,
            CustomUserDetails userDetails
    );

    PayrollResponseDTO getPayrollById(Long payrollId, CustomUserDetails userDetails);

    PayrollResponseDTO updatePayroll(Long payrollId, PayrollRequestDTO request);

    void deletePayroll(Long payrollId);
}
