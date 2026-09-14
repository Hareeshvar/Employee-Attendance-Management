package com.hareeshvar.attendance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hareeshvar.attendance.dto.request.EmployeeWorkplaceAssignmentDTO;
import com.hareeshvar.attendance.dto.request.WorkplaceRequestDTO;
import com.hareeshvar.attendance.dto.response.WorkplaceResponseDTO;
import com.hareeshvar.attendance.entity.EmployeeWorkplace;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.entity.Workplace;
import com.hareeshvar.attendance.exception.BadRequestException;
import com.hareeshvar.attendance.exception.BusinessRuleViolationException;
import com.hareeshvar.attendance.repository.AttendanceLocationVerificationRepository;
import com.hareeshvar.attendance.repository.EmployeeWorkplaceRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.repository.WorkplaceRepository;
import com.hareeshvar.attendance.service.impl.WorkplaceServiceImpl;

@ExtendWith(MockitoExtension.class)
class WorkplaceServiceTest {

    @Mock
    private WorkplaceRepository workplaceRepository;
    @Mock
    private EmployeeWorkplaceRepository employeeWorkplaceRepository;
    @Mock
    private AttendanceLocationVerificationRepository attendanceLocationVerificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;

    private Clock fixedClock;
    private WorkplaceServiceImpl workplaceService;

    private Workplace sampleWorkplace;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-09-14T09:00:00Z"), ZoneId.of("UTC"));
        workplaceService = new WorkplaceServiceImpl(
                workplaceRepository,
                employeeWorkplaceRepository,
                attendanceLocationVerificationRepository,
                userRepository,
                auditLogService,
                fixedClock
        );

        sampleWorkplace = Workplace.builder()
                .workplaceId(1L)
                .name("College Campus")
                .code("SKCET")
                .latitude(BigDecimal.valueOf(10.9390304))
                .longitude(BigDecimal.valueOf(76.9522005))
                .radiusMeters(200.0)
                .maxAccuracyMeters(100.0)
                .isActive(true)
                .build();

        sampleUser = User.builder()
                .userId(5L)
                .username("john_doe")
                .build();
    }

    @Test
    @DisplayName("CreateWorkplace: Successfully creates and saves workplace")
    void testCreateWorkplaceSuccess() {
        WorkplaceRequestDTO req = WorkplaceRequestDTO.builder()
                .name("College Campus")
                .code("SKCET")
                .latitude(BigDecimal.valueOf(10.9390304))
                .longitude(BigDecimal.valueOf(76.9522005))
                .radiusMeters(200.0)
                .maxAccuracyMeters(100.0)
                .isActive(true)
                .build();

        when(workplaceRepository.existsByName(req.getName())).thenReturn(false);
        when(workplaceRepository.existsByCode(req.getCode())).thenReturn(false);
        when(workplaceRepository.save(any(Workplace.class))).thenAnswer(invocation -> {
            Workplace wp = invocation.getArgument(0);
            wp.setWorkplaceId(1L);
            return wp;
        });

        WorkplaceResponseDTO res = workplaceService.createWorkplace(req);
        assertNotNull(res);
        assertEquals("College Campus", res.getName());
        assertEquals("SKCET", res.getCode());
    }

    @Test
    @DisplayName("CreateWorkplace: Duplicate name or code throws BadRequestException")
    void testCreateWorkplaceDuplicates() {
        WorkplaceRequestDTO req = WorkplaceRequestDTO.builder()
                .name("College Campus")
                .code("SKCET")
                .build();

        when(workplaceRepository.existsByName(req.getName())).thenReturn(true);
        assertThrows(BadRequestException.class, () -> workplaceService.createWorkplace(req));

        when(workplaceRepository.existsByName(req.getName())).thenReturn(false);
        when(workplaceRepository.existsByCode(req.getCode())).thenReturn(true);
        assertThrows(BadRequestException.class, () -> workplaceService.createWorkplace(req));
    }

    @Test
    @DisplayName("DeleteWorkplace: Blocked if referenced by historical attendance location verifications")
    void testDeleteWorkplaceBlockedIfReferenced() {
        when(workplaceRepository.findById(1L)).thenReturn(Optional.of(sampleWorkplace));
        when(attendanceLocationVerificationRepository.countByWorkplaceWorkplaceId(1L)).thenReturn(42L);

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                workplaceService.deleteWorkplace(1L));

        assertEquals("Cannot delete workplace with associated historical attendance records. Deactivate the workplace instead.", ex.getMessage());
    }

    @Test
    @DisplayName("DeleteWorkplace: Allowed if unreferenced by attendance verifications")
    void testDeleteWorkplaceAllowedIfUnreferenced() {
        when(workplaceRepository.findById(1L)).thenReturn(Optional.of(sampleWorkplace));
        when(attendanceLocationVerificationRepository.countByWorkplaceWorkplaceId(1L)).thenReturn(0L);
        when(employeeWorkplaceRepository.findByWorkplaceWorkplaceId(1L)).thenReturn(List.of());

        workplaceService.deleteWorkplace(1L);
        verify(workplaceRepository).delete(sampleWorkplace);
    }

    @Test
    @DisplayName("Case I: Re-assignment history - employee can be assigned to same workplace again for a future non-overlapping window")
    void testReassignmentHistoryAllowed() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(sampleUser));
        when(workplaceRepository.findById(1L)).thenReturn(Optional.of(sampleWorkplace));

        // Prior expired assignment from Jan to Jun 2026
        EmployeeWorkplace prior = EmployeeWorkplace.builder()
                .employeeWorkplaceId(10L)
                .user(sampleUser)
                .workplace(sampleWorkplace)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 6, 30))
                .build();

        when(employeeWorkplaceRepository.findByUserUserIdAndWorkplaceWorkplaceIdAndStatus(5L, 1L, "ACTIVE"))
                .thenReturn(List.of(prior));

        // New assignment for 2027 (no overlap with Jan-Jun 2026)
        EmployeeWorkplaceAssignmentDTO newReq = EmployeeWorkplaceAssignmentDTO.builder()
                .userId(5L)
                .workplaceId(1L)
                .effectiveFrom(LocalDate.of(2027, 1, 1))
                .effectiveTo(LocalDate.of(2027, 12, 31))
                .isPrimary(true)
                .build();

        workplaceService.assignEmployee(newReq);

        ArgumentCaptor<EmployeeWorkplace> captor = ArgumentCaptor.forClass(EmployeeWorkplace.class);
        verify(employeeWorkplaceRepository).save(captor.capture());
        assertEquals(LocalDate.of(2027, 1, 1), captor.getValue().getEffectiveFrom());
    }

    @Test
    @DisplayName("Case J: Overlapping assignment for same user and workplace is rejected")
    void testOverlappingAssignmentRejected() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(sampleUser));
        when(workplaceRepository.findById(1L)).thenReturn(Optional.of(sampleWorkplace));

        // Active assignment from Jan to Dec 2026
        EmployeeWorkplace existing = EmployeeWorkplace.builder()
                .employeeWorkplaceId(10L)
                .user(sampleUser)
                .workplace(sampleWorkplace)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 12, 31))
                .build();

        when(employeeWorkplaceRepository.findByUserUserIdAndWorkplaceWorkplaceIdAndStatus(5L, 1L, "ACTIVE"))
                .thenReturn(List.of(existing));

        // Overlapping request from Sep 2026 to Mar 2027
        EmployeeWorkplaceAssignmentDTO overlappingReq = EmployeeWorkplaceAssignmentDTO.builder()
                .userId(5L)
                .workplaceId(1L)
                .effectiveFrom(LocalDate.of(2026, 9, 1))
                .effectiveTo(LocalDate.of(2027, 3, 31))
                .build();

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                workplaceService.assignEmployee(overlappingReq));

        assertTrue(ex.getMessage().contains("already exists within the specified date range"));
    }
}
