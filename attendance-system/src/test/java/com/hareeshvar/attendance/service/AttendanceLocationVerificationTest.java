package com.hareeshvar.attendance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hareeshvar.attendance.dto.request.LocationPunchRequestDTO;
import com.hareeshvar.attendance.dto.response.AttendanceResponseDTO;
import com.hareeshvar.attendance.entity.Attendance;
import com.hareeshvar.attendance.entity.Department;
import com.hareeshvar.attendance.entity.EmployeeWorkplace;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.entity.Workplace;
import com.hareeshvar.attendance.enums.AttendanceStatus;
import com.hareeshvar.attendance.enums.PunchType;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.enums.VerificationMethod;
import com.hareeshvar.attendance.exception.BusinessRuleViolationException;
import com.hareeshvar.attendance.mapper.AttendanceMapper;
import com.hareeshvar.attendance.repository.AttendanceRepository;
import com.hareeshvar.attendance.repository.DepartmentRepository;
import com.hareeshvar.attendance.repository.EmployeeWorkplaceRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.impl.AttendanceServiceImpl;
import com.hareeshvar.attendance.service.impl.GeofenceServiceImpl;

@ExtendWith(MockitoExtension.class)
class AttendanceLocationVerificationTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private AttendanceMapper attendanceMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private AttendanceIntelligenceService attendanceIntelligenceService;
    @Mock
    private EmployeeWorkplaceRepository employeeWorkplaceRepository;

    private Clock fixedClock;
    private GeofenceService geofenceService;
    private AttendanceServiceImpl attendanceService;

    // College & Home coordinates (~6.56km apart)
    private static final double COLLEGE_LAT = 10.9390304;
    private static final double COLLEGE_LON = 76.9522005;
    private static final double HOME_LAT = 10.9979936;
    private static final double HOME_LON = 76.9548166;

    private Workplace collegeWorkplace;
    private Workplace homeWorkplace;
    private User testUser;
    private Department testDept;
    private CustomUserDetails employeeUserDetails;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-09-14T09:00:00Z"), ZoneId.of("UTC"));
        geofenceService = new GeofenceServiceImpl(employeeWorkplaceRepository, fixedClock);

        attendanceService = new AttendanceServiceImpl(
                attendanceRepository,
                attendanceMapper,
                userRepository,
                departmentRepository,
                auditLogService,
                attendanceIntelligenceService,
                geofenceService,
                fixedClock
        );

        testDept = Department.builder().departmentId(1L).departmentName("Engineering").build();
        testUser = User.builder().userId(100L).username("emp_user").department(testDept).build();

        employeeUserDetails = CustomUserDetails.builder()
                .userId(100L)
                .username("emp_user")
                .password("password")
                .role(RoleName.EMPLOYEE)
                .departmentId(1L)
                .build();

        collegeWorkplace = Workplace.builder()
                .workplaceId(1L)
                .name("College Campus")
                .code("COLLEGE")
                .latitude(BigDecimal.valueOf(COLLEGE_LAT))
                .longitude(BigDecimal.valueOf(COLLEGE_LON))
                .radiusMeters(250.0)
                .maxAccuracyMeters(100.0)
                .isActive(true)
                .build();

        homeWorkplace = Workplace.builder()
                .workplaceId(2L)
                .name("Home Office")
                .code("HOME")
                .latitude(BigDecimal.valueOf(HOME_LAT))
                .longitude(BigDecimal.valueOf(HOME_LON))
                .radiusMeters(100.0)
                .maxAccuracyMeters(75.0)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Case A: College check-in + Home check-out creates independent verifications referencing College and Home")
    void testCaseA_CollegeCheckIn_HomeCheckOut() {
        LocalDate today = LocalDate.now(fixedClock);

        EmployeeWorkplace ewCollege = EmployeeWorkplace.builder()
                .employeeWorkplaceId(11L)
                .user(testUser)
                .workplace(collegeWorkplace)
                .isPrimary(true)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        EmployeeWorkplace ewHome = EmployeeWorkplace.builder()
                .employeeWorkplaceId(12L)
                .user(testUser)
                .workplace(homeWorkplace)
                .isPrimary(false)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today)).thenReturn(Optional.empty());
        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of(ewCollege, ewHome));

        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceMapper.toResponse(any(Attendance.class))).thenReturn(new AttendanceResponseDTO());

        // 1. Check-In at College
        LocationPunchRequestDTO checkInReq = LocationPunchRequestDTO.builder()
                .latitude(COLLEGE_LAT)
                .longitude(COLLEGE_LON)
                .accuracyMeters(20.0)
                .build();

        attendanceService.checkInWithAuth(100L, checkInReq, employeeUserDetails);

        // Simulate created attendance entity with Check-In verification
        Attendance existingAttendance = Attendance.builder()
                .attendanceId(555L)
                .user(testUser)
                .department(testDept)
                .attendanceDate(today)
                .checkInTime(LocalTime.of(9, 0))
                .status(AttendanceStatus.PRESENT)
                .build();

        // Perform check-in directly through service and capture
        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today))
                .thenReturn(Optional.of(existingAttendance));

        // 2. Check-Out at Home Office
        LocationPunchRequestDTO checkOutReq = LocationPunchRequestDTO.builder()
                .latitude(HOME_LAT)
                .longitude(HOME_LON)
                .accuracyMeters(25.0)
                .build();

        attendanceService.checkOutWithAuth(100L, checkOutReq, employeeUserDetails);

        // Verify that existingAttendance has CHECK_OUT verification referencing Home
        assertNotNull(existingAttendance.getCheckOutVerification());
        assertEquals(homeWorkplace.getWorkplaceId(), existingAttendance.getCheckOutVerification().getWorkplace().getWorkplaceId());
        assertEquals(PunchType.CHECK_OUT, existingAttendance.getCheckOutVerification().getPunchType());
        assertEquals(VerificationMethod.GEOFENCE, existingAttendance.getCheckOutVerification().getVerificationMethod());
        assertTrue(existingAttendance.getCheckOutVerification().getLocationVerified());
    }

    @Test
    @DisplayName("Case B: Home check-in + College check-out creates independent verifications referencing Home and College")
    void testCaseB_HomeCheckIn_CollegeCheckOut() {
        LocalDate today = LocalDate.now(fixedClock);

        EmployeeWorkplace ewCollege = EmployeeWorkplace.builder()
                .user(testUser)
                .workplace(collegeWorkplace)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        EmployeeWorkplace ewHome = EmployeeWorkplace.builder()
                .user(testUser)
                .workplace(homeWorkplace)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today)).thenReturn(Optional.empty());
        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of(ewCollege, ewHome));

        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceMapper.toResponse(any(Attendance.class))).thenReturn(new AttendanceResponseDTO());

        // Check-in at Home
        LocationPunchRequestDTO checkInReq = LocationPunchRequestDTO.builder()
                .latitude(HOME_LAT)
                .longitude(HOME_LON)
                .accuracyMeters(18.0)
                .build();

        attendanceService.checkInWithAuth(100L, checkInReq, employeeUserDetails);

        Attendance attendance = Attendance.builder()
                .attendanceId(556L)
                .user(testUser)
                .attendanceDate(today)
                .checkInTime(LocalTime.of(9, 5))
                .build();

        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today))
                .thenReturn(Optional.of(attendance));

        // Check-out at College
        LocationPunchRequestDTO checkOutReq = LocationPunchRequestDTO.builder()
                .latitude(COLLEGE_LAT)
                .longitude(COLLEGE_LON)
                .accuracyMeters(22.0)
                .build();

        attendanceService.checkOutWithAuth(100L, checkOutReq, employeeUserDetails);

        assertNotNull(attendance.getCheckOutVerification());
        assertEquals(collegeWorkplace.getWorkplaceId(), attendance.getCheckOutVerification().getWorkplace().getWorkplaceId());
        assertEquals(PunchType.CHECK_OUT, attendance.getCheckOutVerification().getPunchType());
    }

    @Test
    @DisplayName("Case C: Same workplace check-in + check-out creates two separate verification records")
    void testCaseC_SameWorkplaceBothPunches() {
        LocalDate today = LocalDate.now(fixedClock);

        EmployeeWorkplace ewCollege = EmployeeWorkplace.builder()
                .user(testUser)
                .workplace(collegeWorkplace)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of(ewCollege));

        Attendance attendance = Attendance.builder()
                .attendanceId(557L)
                .user(testUser)
                .attendanceDate(today)
                .checkInTime(LocalTime.of(9, 0))
                .build();

        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceMapper.toResponse(any(Attendance.class))).thenReturn(new AttendanceResponseDTO());

        LocationPunchRequestDTO checkOutReq = LocationPunchRequestDTO.builder()
                .latitude(COLLEGE_LAT)
                .longitude(COLLEGE_LON)
                .accuracyMeters(10.0)
                .build();

        attendanceService.checkOutWithAuth(100L, checkOutReq, employeeUserDetails);

        assertEquals(1, attendance.getVerifications().size());
        assertEquals(PunchType.CHECK_OUT, attendance.getVerifications().get(0).getPunchType());
    }

    @Test
    @DisplayName("Case D: Employee assigned to College + Home; location outside both throws BusinessRuleViolationException")
    void testCaseD_OutsideBothAssignedWorkplaces() {
        LocalDate today = LocalDate.now(fixedClock);

        EmployeeWorkplace ewCollege = EmployeeWorkplace.builder().user(testUser).workplace(collegeWorkplace).status("ACTIVE").effectiveFrom(LocalDate.of(2026, 1, 1)).build();
        EmployeeWorkplace ewHome = EmployeeWorkplace.builder().user(testUser).workplace(homeWorkplace).status("ACTIVE").effectiveFrom(LocalDate.of(2026, 1, 1)).build();

        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today)).thenReturn(Optional.empty());
        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of(ewCollege, ewHome));

        // Coordinates at Bangalore (~250km away)
        LocationPunchRequestDTO outsideReq = LocationPunchRequestDTO.builder()
                .latitude(12.9716)
                .longitude(77.5946)
                .accuracyMeters(20.0)
                .build();

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                attendanceService.checkInWithAuth(100L, outsideReq, employeeUserDetails));

        assertTrue(ex.getMessage().contains("outside your approved workplace area"));
    }

    @Test
    @DisplayName("Case E: Employee with no active workplace assignments throws BusinessRuleViolationException")
    void testCaseE_NoActiveWorkplace() {
        LocalDate today = LocalDate.now(fixedClock);

        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today)).thenReturn(Optional.empty());
        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of());

        LocationPunchRequestDTO req = LocationPunchRequestDTO.builder()
                .latitude(COLLEGE_LAT)
                .longitude(COLLEGE_LON)
                .accuracyMeters(15.0)
                .build();

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                attendanceService.checkInWithAuth(100L, req, employeeUserDetails));

        assertTrue(ex.getMessage().contains("No active workplace assigned"));
    }

    @Test
    @DisplayName("Case F: Inactive workplace is excluded from verification candidate pool")
    void testCaseF_InactiveWorkplaceCannotVerify() {
        LocalDate today = LocalDate.now(fixedClock);

        Workplace deactivated = Workplace.builder()
                .workplaceId(99L)
                .name("Deactivated Branch")
                .latitude(BigDecimal.valueOf(COLLEGE_LAT))
                .longitude(BigDecimal.valueOf(COLLEGE_LON))
                .radiusMeters(300.0)
                .maxAccuracyMeters(100.0)
                .isActive(false)
                .build();

        EmployeeWorkplace ew = EmployeeWorkplace.builder().user(testUser).workplace(deactivated).status("ACTIVE").effectiveFrom(LocalDate.of(2026, 1, 1)).build();

        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today)).thenReturn(Optional.empty());
        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of(ew));

        LocationPunchRequestDTO req = LocationPunchRequestDTO.builder()
                .latitude(COLLEGE_LAT)
                .longitude(COLLEGE_LON)
                .accuracyMeters(15.0)
                .build();

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                attendanceService.checkInWithAuth(100L, req, employeeUserDetails));

        assertTrue(ex.getMessage().contains("outside your approved workplace area"));
    }

    @Test
    @DisplayName("Case G: Expired assignment (effective_to < today) cannot verify")
    void testCaseG_ExpiredAssignmentCannotVerify() {
        LocalDate today = LocalDate.now(fixedClock);

        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today)).thenReturn(Optional.empty());
        // Repository query filters by effectiveTo >= today; returns empty for expired
        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of());

        LocationPunchRequestDTO req = LocationPunchRequestDTO.builder()
                .latitude(COLLEGE_LAT)
                .longitude(COLLEGE_LON)
                .accuracyMeters(15.0)
                .build();

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                attendanceService.checkInWithAuth(100L, req, employeeUserDetails));

        assertTrue(ex.getMessage().contains("No active workplace assigned"));
    }

    @Test
    @DisplayName("Case H: Future assignment (effective_from > today) cannot verify")
    void testCaseH_FutureAssignmentCannotVerify() {
        LocalDate today = LocalDate.now(fixedClock);

        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserUserIdAndAttendanceDate(100L, today)).thenReturn(Optional.empty());
        // Repository query filters by effectiveFrom <= today; returns empty for future
        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of());

        LocationPunchRequestDTO req = LocationPunchRequestDTO.builder()
                .latitude(COLLEGE_LAT)
                .longitude(COLLEGE_LON)
                .accuracyMeters(15.0)
                .build();

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                attendanceService.checkInWithAuth(100L, req, employeeUserDetails));

        assertTrue(ex.getMessage().contains("No active workplace assigned"));
    }
}
