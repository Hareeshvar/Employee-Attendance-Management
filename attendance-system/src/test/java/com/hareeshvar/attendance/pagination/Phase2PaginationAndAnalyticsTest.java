package com.hareeshvar.attendance.pagination;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.entity.Attendance;
import com.hareeshvar.attendance.entity.Department;
import com.hareeshvar.attendance.entity.Designation;
import com.hareeshvar.attendance.entity.Role;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.AttendanceStatus;
import com.hareeshvar.attendance.enums.DepartmentStatus;
import com.hareeshvar.attendance.enums.Gender;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.enums.UserStatus;
import com.hareeshvar.attendance.repository.AttendanceRepository;
import com.hareeshvar.attendance.repository.DepartmentRepository;
import com.hareeshvar.attendance.repository.DesignationRepository;
import com.hareeshvar.attendance.repository.RoleRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.security.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Phase2PaginationAndAnalyticsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User adminUser;
    private User employeeUser;
    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findAll().stream().findFirst().orElseGet(() ->
                departmentRepository.save(Department.builder().departmentName("Engineering").departmentCode("ENG").status(DepartmentStatus.ACTIVE).build()));

        Designation desig = designationRepository.findAll().stream().findFirst().orElseGet(() ->
                designationRepository.save(Designation.builder().designationName("Engineer").build()));

        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN).orElseGet(() -> roleRepository.save(Role.builder().roleName(RoleName.ADMIN).build()));
        Role empRole = roleRepository.findByRoleName(RoleName.EMPLOYEE).orElseGet(() -> roleRepository.save(Role.builder().roleName(RoleName.EMPLOYEE).build()));

        adminUser = userRepository.findByUsername("phase2_admin").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("phase2_admin")
                        .email("phase2_admin@test.com")
                        .password(passwordEncoder.encode("password123"))
                        .firstName("Phase2")
                        .lastName("Admin")
                        .gender(Gender.MALE)
                        .status(UserStatus.ACTIVE)
                        .role(adminRole)
                        .department(dept)
                        .designation(desig)
                        .build()));

        employeeUser = userRepository.findByUsername("phase2_emp").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("phase2_emp")
                        .email("phase2_emp@test.com")
                        .password(passwordEncoder.encode("password123"))
                        .firstName("Phase2")
                        .lastName("Emp")
                        .gender(Gender.MALE)
                        .status(UserStatus.ACTIVE)
                        .role(empRole)
                        .department(dept)
                        .designation(desig)
                        .build()));

        adminToken = jwtService.generateToken(CustomUserDetails.create(adminUser));
        employeeToken = jwtService.generateToken(CustomUserDetails.create(employeeUser));
    }

    @Test
    @DisplayName("Verify server-side pagination structure on /api/v1/users")
    void testUsersPaginationStructure() throws Exception {
        mockMvc.perform(get("/api/v1/users?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    @DisplayName("Verify attendance analytics endpoint returns all >100 records without capping")
    void testAnalyticsEndpointReturnsAllRecordsOver100() throws Exception {
        // Generate 105 attendance records for employeeUser
        LocalDate startDate = LocalDate.now().minusDays(110);
        for (int i = 0; i < 105; i++) {
            Attendance attendance = Attendance.builder()
                    .user(employeeUser)
                    .department(employeeUser.getDepartment())
                    .attendanceDate(startDate.plusDays(i))
                    .checkInTime(LocalTime.of(9, 0))
                    .checkOutTime(LocalTime.of(17, 0))
                    .workingHours(8.0)
                    .status(AttendanceStatus.PRESENT)
                    .build();
            attendanceRepository.save(attendance);
        }

        mockMvc.perform(get("/api/v1/attendance/analytics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(105)));
    }
}
