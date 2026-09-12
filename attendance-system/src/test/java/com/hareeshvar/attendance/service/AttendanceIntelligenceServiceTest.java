package com.hareeshvar.attendance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hareeshvar.attendance.entity.Attendance;
import com.hareeshvar.attendance.entity.Department;
import com.hareeshvar.attendance.entity.Designation;
import com.hareeshvar.attendance.entity.EmployeeShift;
import com.hareeshvar.attendance.entity.Role;
import com.hareeshvar.attendance.entity.Shift;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.AttendanceStatus;
import com.hareeshvar.attendance.enums.DepartmentStatus;
import com.hareeshvar.attendance.enums.EmployeeShiftStatus;
import com.hareeshvar.attendance.enums.Gender;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.enums.UserStatus;
import com.hareeshvar.attendance.repository.AttendanceRepository;
import com.hareeshvar.attendance.repository.DepartmentRepository;
import com.hareeshvar.attendance.repository.DesignationRepository;
import com.hareeshvar.attendance.repository.EmployeeShiftRepository;
import com.hareeshvar.attendance.repository.RoleRepository;
import com.hareeshvar.attendance.repository.ShiftRepository;
import com.hareeshvar.attendance.repository.UserRepository;

@SpringBootTest
@Transactional
public class AttendanceIntelligenceServiceTest {

    @Autowired
    private AttendanceIntelligenceService attendanceIntelligenceService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private EmployeeShiftRepository employeeShiftRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Shift dayShift;
    private Shift nightShift;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findAll().stream().findFirst().orElseGet(() ->
                departmentRepository.save(Department.builder().departmentName("Tech").departmentCode("TCH").status(DepartmentStatus.ACTIVE).build()));

        Designation desig = designationRepository.findAll().stream().findFirst().orElseGet(() ->
                designationRepository.save(Designation.builder().designationName("Dev").build()));

        Role role = roleRepository.findByRoleName(RoleName.EMPLOYEE).orElseGet(() ->
                roleRepository.save(Role.builder().roleName(RoleName.EMPLOYEE).build()));

        testUser = userRepository.save(User.builder()
                .username("intel_user_" + System.currentTimeMillis())
                .email("intel_" + System.currentTimeMillis() + "@test.com")
                .password("pass123")
                .firstName("Intel")
                .lastName("User")
                .gender(Gender.MALE)
                .status(UserStatus.ACTIVE)
                .role(role)
                .department(dept)
                .designation(desig)
                .build());

        dayShift = shiftRepository.save(Shift.builder()
                .shiftName("Day Shift " + System.currentTimeMillis())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .workingHours(8)
                .graceMinutes(15)
                .build());

        nightShift = shiftRepository.save(Shift.builder()
                .shiftName("Night Shift " + System.currentTimeMillis())
                .startTime(LocalTime.of(22, 0))
                .endTime(LocalTime.of(6, 0))
                .workingHours(8)
                .graceMinutes(15)
                .build());
    }

    @Test
    @DisplayName("Verify on-time check-in within grace period yields PRESENT and 0 lateMinutes")
    void testOnTimeCheckInWithinGrace() throws Exception {
        employeeShiftRepository.save(EmployeeShift.builder()
                .user(testUser)
                .shift(dayShift)
                .effectiveDate(LocalDate.now().minusDays(1))
                .status(EmployeeShiftStatus.ACTIVE)
                .build());

        Attendance attendance = Attendance.builder()
                .user(testUser)
                .department(testUser.getDepartment())
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(9, 10)) // 10 mins late, grace is 15 -> On time!
                .build();

        attendanceIntelligenceService.processAttendanceIntelligence(attendance);

        assertEquals(AttendanceStatus.PRESENT, attendance.getStatus());
        assertEquals(0, attendance.getLateMinutes());
        List<String> exceptions = objectMapper.readValue(attendance.getExceptionsJson(), new TypeReference<List<String>>() {});
        assertFalse(exceptions.contains("LATE_ARRIVAL"));
    }

    @Test
    @DisplayName("Verify check-in past grace period yields LATE status and exact lateMinutes")
    void testLateCheckInPastGrace() throws Exception {
        employeeShiftRepository.save(EmployeeShift.builder()
                .user(testUser)
                .shift(dayShift)
                .effectiveDate(LocalDate.now().minusDays(1))
                .status(EmployeeShiftStatus.ACTIVE)
                .build());

        Attendance attendance = Attendance.builder()
                .user(testUser)
                .department(testUser.getDepartment())
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(9, 20)) // 20 mins late, exceeds 15 grace
                .build();

        attendanceIntelligenceService.processAttendanceIntelligence(attendance);

        assertEquals(AttendanceStatus.LATE, attendance.getStatus());
        assertEquals(20, attendance.getLateMinutes());
        List<String> exceptions = objectMapper.readValue(attendance.getExceptionsJson(), new TypeReference<List<String>>() {});
        assertTrue(exceptions.contains("LATE_ARRIVAL"));
    }

    @Test
    @DisplayName("Verify early departure calculation when checkout is before shift end")
    void testEarlyDepartureCalculation() throws Exception {
        employeeShiftRepository.save(EmployeeShift.builder()
                .user(testUser)
                .shift(dayShift)
                .effectiveDate(LocalDate.now().minusDays(1))
                .status(EmployeeShiftStatus.ACTIVE)
                .build());

        Attendance attendance = Attendance.builder()
                .user(testUser)
                .department(testUser.getDepartment())
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(9, 0))
                .checkOutTime(LocalTime.of(16, 30)) // 30 mins early departure (shift ends 17:00)
                .build();

        attendanceIntelligenceService.processAttendanceIntelligence(attendance);

        assertEquals(450, attendance.getWorkingMinutes()); // 7.5 hrs = 450 mins
        assertEquals(7.5, attendance.getWorkingHours(), 0.001);
        assertEquals(30, attendance.getEarlyDepartureMinutes());
        assertEquals(0, attendance.getOvertimeMinutes());

        List<String> exceptions = objectMapper.readValue(attendance.getExceptionsJson(), new TypeReference<List<String>>() {});
        assertTrue(exceptions.contains("EARLY_DEPARTURE"));
    }

    @Test
    @DisplayName("Verify overtime duration calculation when worked minutes exceed scheduled minutes")
    void testOvertimeCalculation() throws Exception {
        employeeShiftRepository.save(EmployeeShift.builder()
                .user(testUser)
                .shift(dayShift)
                .effectiveDate(LocalDate.now().minusDays(1))
                .status(EmployeeShiftStatus.ACTIVE)
                .build());

        Attendance attendance = Attendance.builder()
                .user(testUser)
                .department(testUser.getDepartment())
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(9, 0))
                .checkOutTime(LocalTime.of(19, 30)) // Worked 10.5 hrs (630 mins), scheduled is 8 hrs (480 mins) -> 150 mins overtime
                .build();

        attendanceIntelligenceService.processAttendanceIntelligence(attendance);

        assertEquals(630, attendance.getWorkingMinutes());
        assertEquals(10.5, attendance.getWorkingHours(), 0.001);
        assertEquals(0, attendance.getEarlyDepartureMinutes());
        assertEquals(150, attendance.getOvertimeMinutes());

        List<String> exceptions = objectMapper.readValue(attendance.getExceptionsJson(), new TypeReference<List<String>>() {});
        assertTrue(exceptions.contains("OVERTIME"));
    }

    @Test
    @DisplayName("Verify overnight shift math correctly calculates across midnight boundary without negative values")
    void testOvernightShiftDurationAndBoundaries() throws Exception {
        employeeShiftRepository.save(EmployeeShift.builder()
                .user(testUser)
                .shift(nightShift) // 22:00 to 06:00
                .effectiveDate(LocalDate.now().minusDays(1))
                .status(EmployeeShiftStatus.ACTIVE)
                .build());

        Attendance attendance = Attendance.builder()
                .user(testUser)
                .department(testUser.getDepartment())
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(22, 10)) // On-time (within 15m grace)
                .checkOutTime(LocalTime.of(6, 30)) // Next day checkout -> Worked 8h 20m = 500 mins
                .build();

        attendanceIntelligenceService.processAttendanceIntelligence(attendance);

        assertEquals(AttendanceStatus.PRESENT, attendance.getStatus());
        assertEquals(500, attendance.getWorkingMinutes());
        assertEquals(20, attendance.getOvertimeMinutes()); // Scheduled is 480 mins (8h)
        assertEquals(0, attendance.getEarlyDepartureMinutes());
        assertEquals(0, attendance.getLateMinutes());
    }

    @Test
    @DisplayName("Verify unassigned shift employee logs attendance safely with NO_SHIFT_ASSIGNED flag")
    void testUnassignedShiftHandling() throws Exception {
        // No EmployeeShift saved for testUser

        Attendance attendance = Attendance.builder()
                .user(testUser)
                .department(testUser.getDepartment())
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(9, 30))
                .checkOutTime(LocalTime.of(17, 30))
                .build();

        attendanceIntelligenceService.processAttendanceIntelligence(attendance);

        assertEquals(AttendanceStatus.PRESENT, attendance.getStatus());
        assertNull(attendance.getShift());
        assertEquals(0, attendance.getLateMinutes());
        assertEquals(0, attendance.getEarlyDepartureMinutes());
        assertEquals(0, attendance.getOvertimeMinutes());
        assertEquals(480, attendance.getWorkingMinutes());

        List<String> exceptions = objectMapper.readValue(attendance.getExceptionsJson(), new TypeReference<List<String>>() {});
        assertTrue(exceptions.contains("NO_SHIFT_ASSIGNED"));
    }
}
