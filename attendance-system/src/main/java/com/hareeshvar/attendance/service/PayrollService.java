package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.PayrollRequestDTO;
import com.hareeshvar.attendance.dto.response.PayrollResponseDTO;

public interface PayrollService {

    PayrollResponseDTO createPayroll(PayrollRequestDTO request);

    List<PayrollResponseDTO> getAllPayrolls();

    PayrollResponseDTO getPayrollById(Long payrollId);

    PayrollResponseDTO updatePayroll(Long payrollId, PayrollRequestDTO request);

    void deletePayroll(Long payrollId);

}
