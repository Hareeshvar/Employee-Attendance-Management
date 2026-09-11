package com.hareeshvar.attendance.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hareeshvar.attendance.dto.request.LeaveRequestDTO;
import com.hareeshvar.attendance.entity.Department;
import com.hareeshvar.attendance.entity.Designation;
import com.hareeshvar.attendance.entity.Role;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.DepartmentStatus;
import com.hareeshvar.attendance.enums.Gender;
import com.hareeshvar.attendance.enums.LeaveType;
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
public class LeaveControllerTest {

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

    private User employeeUser;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findAll().stream().findFirst().orElseGet(() ->
                departmentRepository.save(Department.builder().departmentName("Engineering").departmentCode("ENG").status(DepartmentStatus.ACTIVE).build()));

        Designation desig = designationRepository.findAll().stream().findFirst().orElseGet(() ->
                designationRepository.save(Designation.builder().designationName("Engineer").build()));

        Role empRole = roleRepository.findByRoleName(RoleName.EMPLOYEE).orElseGet(() -> roleRepository.save(Role.builder().roleName(RoleName.EMPLOYEE).build()));

        employeeUser = userRepository.findByUsername("test_leave_emp").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("test_leave_emp")
                        .email("leave_emp@test.com")
                        .password(passwordEncoder.encode("password123"))
                        .firstName("Leave")
                        .lastName("Employee")
                        .gender(Gender.MALE)
                        .status(UserStatus.ACTIVE)
                        .role(empRole)
                        .department(dept)
                        .designation(desig)
                        .build()));

        employeeToken = jwtService.generateToken(CustomUserDetails.create(employeeUser));
    }

    @Test
    @DisplayName("Apply for leave test")
    void testApplyLeave() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setUserId(employeeUser.getUserId());
        dto.setLeaveType(LeaveType.SICK);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());
        dto.setReason("Doctor appointment");

        MvcResult result = mockMvc.perform(post("/api/v1/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        System.out.println("RESPONSE STATUS: " + result.getResponse().getStatus());
        System.out.println("RESPONSE BODY: " + result.getResponse().getContentAsString());
    }
}
