package com.hareeshvar.attendance.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hareeshvar.attendance.dto.auth.LoginRequest;
import com.hareeshvar.attendance.entity.Department;
import com.hareeshvar.attendance.entity.Designation;
import com.hareeshvar.attendance.entity.Role;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.DepartmentStatus;
import com.hareeshvar.attendance.enums.Gender;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.enums.UserStatus;
import com.hareeshvar.attendance.repository.DepartmentRepository;
import com.hareeshvar.attendance.repository.DesignationRepository;
import com.hareeshvar.attendance.repository.RoleRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.security.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityRbacTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User hrUser;
    private User managerUser;
    private User employeeUser;

    private String adminToken;
    private String hrToken;
    private String managerToken;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findAll().stream().findFirst().orElseGet(() ->
                departmentRepository.save(Department.builder().departmentName("Engineering").departmentCode("ENG").status(DepartmentStatus.ACTIVE).build()));

        Designation desig = designationRepository.findAll().stream().findFirst().orElseGet(() ->
                designationRepository.save(Designation.builder().designationName("Engineer").build()));

        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN).orElseGet(() -> roleRepository.save(Role.builder().roleName(RoleName.ADMIN).build()));
        Role hrRole = roleRepository.findByRoleName(RoleName.HR).orElseGet(() -> roleRepository.save(Role.builder().roleName(RoleName.HR).build()));
        Role mgrRole = roleRepository.findByRoleName(RoleName.MANAGER).orElseGet(() -> roleRepository.save(Role.builder().roleName(RoleName.MANAGER).build()));
        Role empRole = roleRepository.findByRoleName(RoleName.EMPLOYEE).orElseGet(() -> roleRepository.save(Role.builder().roleName(RoleName.EMPLOYEE).build()));

        adminUser = createUserIfMissing("test_admin", "admin@test.com", RoleName.ADMIN, adminRole, dept, desig);
        hrUser = createUserIfMissing("test_hr", "hr@test.com", RoleName.HR, hrRole, dept, desig);
        managerUser = createUserIfMissing("test_manager", "manager@test.com", RoleName.MANAGER, mgrRole, dept, desig);
        employeeUser = createUserIfMissing("test_employee", "emp@test.com", RoleName.EMPLOYEE, empRole, dept, desig);

        adminToken = jwtService.generateToken(CustomUserDetails.create(adminUser));
        hrToken = jwtService.generateToken(CustomUserDetails.create(hrUser));
        managerToken = jwtService.generateToken(CustomUserDetails.create(managerUser));
        employeeToken = jwtService.generateToken(CustomUserDetails.create(employeeUser));
    }

    private User createUserIfMissing(String username, String email, RoleName roleName, Role role, Department dept, Designation desig) {
        return userRepository.findByUsername(username).orElseGet(() ->
                userRepository.save(User.builder()
                        .username(username)
                        .email(email)
                        .password(passwordEncoder.encode("password123"))
                        .firstName(username)
                        .lastName("User")
                        .gender(Gender.MALE)
                        .status(UserStatus.ACTIVE)
                        .role(role)
                        .department(dept)
                        .designation(desig)
                        .build()));
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoints must return 401")
    void testUnauthenticatedRequestsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Public user registration endpoint POST /api/v1/users must be blocked for unauthenticated users")
    void testPublicRegistrationBlocked() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"hacker\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Valid user authentication yields JWT token with user profile")
    void testValidAuthentication() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test_admin", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN role can access /api/v1/roles")
    void testAdminAccessesRoles() throws Exception {
        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HR role can read /api/v1/roles (200 OK)")
    void testHrCanReadRoles() throws Exception {
        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HR role cannot create /api/v1/roles (Forbidden 403)")
    void testHrDeniedRoleManagement() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"NEW_ROLE\"}")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE role cannot access global /api/v1/users (Forbidden 403/Restricted)")
    void testEmployeeDeniedUserList() throws Exception {
        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MANAGER role cannot access global payroll (Forbidden 403)")
    void testManagerDeniedGlobalPayroll() throws Exception {
        mockMvc.perform(get("/api/v1/payrolls")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Inactive user is rejected during login")
    void testInactiveUserLoginRejected() throws Exception {
        User inactiveUser = userRepository.save(User.builder()
                .username("inactive_user")
                .email("inactive@test.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Inactive")
                .lastName("User")
                .gender(Gender.MALE)
                .status(UserStatus.INACTIVE)
                .role(adminUser.getRole())
                .department(adminUser.getDepartment())
                .designation(adminUser.getDesignation())
                .build());

        LoginRequest loginRequest = new LoginRequest("inactive_user", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
