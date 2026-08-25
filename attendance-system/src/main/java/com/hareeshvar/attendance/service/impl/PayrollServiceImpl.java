package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hareeshvar.attendance.dto.request.PayrollRequestDTO;
import com.hareeshvar.attendance.dto.response.PayrollResponseDTO;
import com.hareeshvar.attendance.entity.Payroll;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.PayrollMapper;
import com.hareeshvar.attendance.repository.PayrollRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.service.PayrollService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final PayrollMapper payrollMapper;

    @Override
    public PayrollResponseDTO createPayroll(PayrollRequestDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + request.getUserId()));

        Payroll payroll = payrollMapper.toEntity(request);
        payroll.setUser(user);

        Payroll savedPayroll = payrollRepository.save(payroll);

        return payrollMapper.toResponse(savedPayroll);
    }

    @Override
    public List<PayrollResponseDTO> getAllPayrolls() {

        return payrollRepository.findAll()
                .stream()
                .map(payrollMapper::toResponse)
                .toList();
    }

    @Override
    public PayrollResponseDTO getPayrollById(Long payrollId) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payroll not found with id : " + payrollId));

        return payrollMapper.toResponse(payroll);
    }

    @Override
    public PayrollResponseDTO updatePayroll(Long payrollId, PayrollRequestDTO request) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payroll not found with id : " + payrollId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + request.getUserId()));

        payroll.setUser(user);
        payroll.setMonth(request.getMonth());
        payroll.setYear(request.getYear());
        payroll.setBasicSalary(request.getBasicSalary());
        payroll.setBonus(request.getBonus());
        payroll.setDeduction(request.getDeduction());
        payroll.setNetSalary(request.getNetSalary());

        Payroll updatedPayroll = payrollRepository.save(payroll);

        return payrollMapper.toResponse(updatedPayroll);
    }

    @Override
    public void deletePayroll(Long payrollId) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payroll not found with id : " + payrollId));

        payrollRepository.delete(payroll);
    }
}
