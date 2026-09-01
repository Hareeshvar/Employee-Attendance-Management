package com.hareeshvar.attendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.sync-dev-passwords:true}")
    private boolean syncDevPasswords;

    @Value("${app.default-admin.username:admin}")
    private String adminUsername;
    @Value("${app.default-admin.password:admin123}")
    private String adminPassword;
    @Value("${app.default-admin.email:admin@attendance.com}")
    private String adminEmail;

    @Value("${app.default-hr.username:hr}")
    private String hrUsername;
    @Value("${app.default-hr.password:hr123}")
    private String hrPassword;
    @Value("${app.default-hr.email:hr@attendance.com}")
    private String hrEmail;

    @Value("${app.default-manager.username:manager}")
    private String managerUsername;
    @Value("${app.default-manager.password:manager123}")
    private String managerPassword;
    @Value("${app.default-manager.email:manager@attendance.com}")
    private String managerEmail;

    @Value("${app.default-employee.username:employee}")
    private String employeeUsername;
    @Value("${app.default-employee.password:employee123}")
    private String employeePassword;
    @Value("${app.default-employee.email:employee@attendance.com}")
    private String employeeEmail;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking data initialization...");

        // 1. Initialize Roles
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .roleName(roleName)
                        .description("System Role: " + roleName.name())
                        .build();
                roleRepository.save(role);
                log.info("Initialized role: {}", roleName);
            }
        }

        // 2. Initialize Default Departments
        Department hrDept = departmentRepository.findByDepartmentName("Human Resources").orElse(null);
        if (hrDept == null) {
            hrDept = Department.builder()
                    .departmentName("Human Resources")
                    .departmentCode("HR01")
                    .description("Human Resources & Administration")
                    .status(DepartmentStatus.ACTIVE)
                    .build();
            hrDept = departmentRepository.save(hrDept);
        }

        Department engineeringDept = departmentRepository.findByDepartmentName("Engineering").orElse(null);
        if (engineeringDept == null) {
            engineeringDept = Department.builder()
                    .departmentName("Engineering")
                    .departmentCode("ENG01")
                    .description("Software & Product Engineering")
                    .status(DepartmentStatus.ACTIVE)
                    .build();
            engineeringDept = departmentRepository.save(engineeringDept);
        }

        // 3. Initialize Default Designations
        Designation adminDesig = designationRepository.findByDesignationName("System Administrator").orElse(null);
        if (adminDesig == null) {
            adminDesig = Designation.builder()
                    .designationName("System Administrator")
                    .description("System Administrator")
                    .build();
            adminDesig = designationRepository.save(adminDesig);
        }

        Designation hrDesig = designationRepository.findByDesignationName("HR Manager").orElse(null);
        if (hrDesig == null) {
            hrDesig = Designation.builder()
                    .designationName("HR Manager")
                    .description("HR Lead")
                    .build();
            hrDesig = designationRepository.save(hrDesig);
        }

        Designation mgrDesig = designationRepository.findByDesignationName("Engineering Manager").orElse(null);
        if (mgrDesig == null) {
            mgrDesig = Designation.builder()
                    .designationName("Engineering Manager")
                    .description("Team Manager")
                    .build();
            mgrDesig = designationRepository.save(mgrDesig);
        }

        Designation empDesig = designationRepository.findByDesignationName("Software Engineer").orElse(null);
        if (empDesig == null) {
            empDesig = Designation.builder()
                    .designationName("Software Engineer")
                    .description("Software Developer")
                    .build();
            empDesig = designationRepository.save(empDesig);
        }

        // 4. Seed or synchronize development accounts
        syncSeedUser(adminUsername, adminEmail, adminPassword, "System", "Admin", RoleName.ADMIN, hrDept, adminDesig);
        syncSeedUser(hrUsername, hrEmail, hrPassword, "HR", "Lead", RoleName.HR, hrDept, hrDesig);
        syncSeedUser(managerUsername, managerEmail, managerPassword, "Team", "Manager", RoleName.MANAGER, engineeringDept, mgrDesig);
        syncSeedUser(employeeUsername, employeeEmail, employeePassword, "John", "Doe", RoleName.EMPLOYEE, engineeringDept, empDesig);
    }

    private void syncSeedUser(String username, String email, String password, String firstName, String lastName,
                              RoleName roleName, Department department, Designation designation) {

        User existingUser = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(email).orElse(null));

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        if (existingUser != null) {
            boolean updated = false;
            if (existingUser.getStatus() != UserStatus.ACTIVE) {
                existingUser.setStatus(UserStatus.ACTIVE);
                updated = true;
            }
            if (existingUser.getRole() == null || existingUser.getRole().getRoleName() != roleName) {
                existingUser.setRole(role);
                updated = true;
            }
            if (syncDevPasswords && !passwordEncoder.matches(password, existingUser.getPassword())) {
                existingUser.setPassword(passwordEncoder.encode(password));
                updated = true;
                log.info("Synchronized password for dev user '{}' to match configured default.", username);
            }
            if (updated) {
                userRepository.save(existingUser);
            }
            log.info("Dev account '{}' is verified active with role {}", username, roleName);
            return;
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .gender(Gender.MALE)
                .status(UserStatus.ACTIVE)
                .role(role)
                .department(department)
                .designation(designation)
                .build();

        userRepository.save(user);
        log.info("Successfully bootstrapped seed account: {} ({})", username, roleName);
    }
}
