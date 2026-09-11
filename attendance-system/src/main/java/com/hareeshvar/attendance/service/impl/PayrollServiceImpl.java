package com.hareeshvar.attendance.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.PayrollRequestDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.dto.response.PayrollResponseDTO;
import com.hareeshvar.attendance.entity.Payroll;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.PayrollMapper;
import com.hareeshvar.attendance.repository.PayrollRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.repository.specification.PayrollSpecification;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.AuditLogService;
import com.hareeshvar.attendance.service.PayrollService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final PayrollMapper payrollMapper;
    private final AuditLogService auditLogService;

    @Override
    public PayrollResponseDTO createPayroll(PayrollRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Payroll payroll = payrollMapper.toEntity(request);
        payroll.setUser(user);

        Payroll savedPayroll = payrollRepository.save(payroll);

        auditLogService.logSuccess(
                AuditAction.PAYROLL_CREATED,
                "PAYROLL",
                String.valueOf(savedPayroll.getPayrollId()),
                "Created payroll record for user '" + user.getUsername() + "' (" + savedPayroll.getMonth() + " " + savedPayroll.getYear() + ")",
                Map.of("month", savedPayroll.getMonth(), "year", savedPayroll.getYear(), "username", user.getUsername())
        );

        return payrollMapper.toResponse(savedPayroll);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponseDTO> getAllPayrolls(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.ADMIN || role == RoleName.HR) {
            return payrollRepository.findAll().stream().map(payrollMapper::toResponse).toList();
        }

        if (role == RoleName.EMPLOYEE) {
            return payrollRepository.findByUserUserId(userDetails.getUserId())
                    .stream().map(payrollMapper::toResponse).toList();
        }

        throw new AccessDeniedException("Access denied: Managers do not have access to global payroll data");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PayrollResponseDTO> getPayrollsPaginated(
            Pageable pageable,
            String search,
            String month,
            Integer year,
            CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        if (userDetails.getRole() == RoleName.MANAGER) {
            throw new AccessDeniedException("Access denied: Managers do not have access to global payroll data");
        }

        Specification<Payroll> spec = PayrollSpecification.filterPayrolls(
                search,
                month,
                year,
                userDetails.getRole(),
                userDetails.getDepartmentId(),
                userDetails.getUserId()
        );

        Page<Payroll> page = payrollRepository.findAll(spec, pageable);
        Page<PayrollResponseDTO> dtoPage = page.map(payrollMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollResponseDTO getPayrollById(Long payrollId, CustomUserDetails userDetails) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll", "id", payrollId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.ADMIN || role == RoleName.HR) {
            return payrollMapper.toResponse(payroll);
        }

        if (role == RoleName.EMPLOYEE) {
            if (!payroll.getUser().getUserId().equals(userDetails.getUserId())) {
                throw new AccessDeniedException("Access denied: You can only view your own payroll record");
            }
            return payrollMapper.toResponse(payroll);
        }

        throw new AccessDeniedException("Access denied: Insufficient permissions to view payroll");
    }

    @Override
    public PayrollResponseDTO updatePayroll(Long payrollId, PayrollRequestDTO request) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll", "id", payrollId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        payroll.setUser(user);
        payroll.setMonth(request.getMonth());
        payroll.setYear(request.getYear());
        payroll.setBasicSalary(request.getBasicSalary());
        payroll.setBonus(request.getBonus());
        payroll.setDeduction(request.getDeduction());
        payroll.setNetSalary(request.getNetSalary());

        Payroll updatedPayroll = payrollRepository.save(payroll);

        auditLogService.logSuccess(
                AuditAction.PAYROLL_UPDATED,
                "PAYROLL",
                String.valueOf(updatedPayroll.getPayrollId()),
                "Updated payroll record for user '" + user.getUsername() + "' (" + updatedPayroll.getMonth() + " " + updatedPayroll.getYear() + ")",
                Map.of("month", updatedPayroll.getMonth(), "year", updatedPayroll.getYear(), "username", user.getUsername())
        );

        return payrollMapper.toResponse(updatedPayroll);
    }

    @Override
    public void deletePayroll(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll", "id", payrollId));
        payrollRepository.delete(payroll);

        auditLogService.logSuccess(
                AuditAction.PAYROLL_DELETED,
                "PAYROLL",
                String.valueOf(payrollId),
                "Deleted payroll record #" + payrollId,
                Map.of("payrollId", payrollId)
        );
    }
}
