package com.hareeshvar.attendance.config;

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

        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        // 2. Initialize Department if empty
        Department defaultDepartment;
        if (departmentRepository.count() == 0) {
            defaultDepartment = Department.builder()
                    .departmentName("Administration & HR")
                    .departmentCode("HR01")
                    .description("Default Administration Department")
                    .status(DepartmentStatus.ACTIVE)
                    .build();
            defaultDepartment = departmentRepository.save(defaultDepartment);
            log.info("Initialized default department");
        } else {
            defaultDepartment = departmentRepository.findAll().get(0);
        }

        // 3. Initialize Designation if empty
        Designation defaultDesignation;
        if (designationRepository.count() == 0) {
            defaultDesignation = Designation.builder()
                    .designationName("System Administrator")
                    .description("Default Admin Designation")
                    .build();
            defaultDesignation = designationRepository.save(defaultDesignation);
            log.info("Initialized default designation");
        } else {
            defaultDesignation = designationRepository.findAll().get(0);
        }

        // 4. Initialize or reset default user 727724eucj015@skcet.ac.in
        String targetEmail = "727724eucj015@skcet.ac.in";
        String targetUsername = "727724eucj015";

        User existingUser = userRepository.findByEmail(targetEmail)
                .orElseGet(() -> userRepository.findByUsername(targetUsername).orElse(null));

        if (existingUser == null) {
            User defaultUser = User.builder()
                    .firstName("Hareesh")
                    .lastName("User")
                    .username(targetUsername)
                    .email(targetEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .gender(Gender.MALE)
                    .status(UserStatus.ACTIVE)
                    .role(adminRole)
                    .department(defaultDepartment)
                    .designation(defaultDesignation)
                    .build();
            userRepository.save(defaultUser);
            log.info("Initialized default user: {} with password: admin123", targetEmail);
        } else {
            existingUser.setPassword(passwordEncoder.encode("admin123"));
            existingUser.setRole(adminRole);
            userRepository.save(existingUser);
            log.info("Reset password for user: {} to admin123", targetEmail);
        }

        // Also ensure fallback 'admin' account exists and has password 'admin123'
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser == null) {
            adminUser = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .username("admin")
                    .email("admin@attendance.com")
                    .password(passwordEncoder.encode("admin123"))
                    .gender(Gender.MALE)
                    .status(UserStatus.ACTIVE)
                    .role(adminRole)
                    .department(defaultDepartment)
                    .designation(defaultDesignation)
                    .build();
            userRepository.save(adminUser);
            log.info("Initialized fallback admin account: username 'admin', password 'admin123'");
        } else {
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(adminUser);
            log.info("Reset password for admin user to admin123");
        }
    }
}
