package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.hareeshvar.attendance.dto.request.UserRequestDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.dto.response.UserResponseDTO;
import com.hareeshvar.attendance.entity.Department;
import com.hareeshvar.attendance.entity.Designation;
import com.hareeshvar.attendance.entity.Role;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.enums.UserStatus;
import com.hareeshvar.attendance.exception.ResourceAlreadyExistsException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.UserMapper;
import com.hareeshvar.attendance.repository.DepartmentRepository;
import com.hareeshvar.attendance.repository.DesignationRepository;
import com.hareeshvar.attendance.repository.RoleRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.repository.specification.UserSpecification;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO createUser(UserRequestDTO request, CustomUserDetails creator) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        // HR cannot create ADMIN accounts
        if (role.getRoleName() == RoleName.ADMIN && (creator == null || creator.getRole() != RoleName.ADMIN)) {
            throw new AccessDeniedException("Access denied: HR cannot create ADMIN accounts");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId()));

        User user = userMapper.toEntity(request);
        user.setRole(role);
        user.setDepartment(department);
        user.setDesignation(designation);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        List<User> list;

        if (role == RoleName.ADMIN || role == RoleName.HR) {
            list = userRepository.findAll();
        } else if (role == RoleName.MANAGER) {
            Long deptId = userDetails.getDepartmentId();
            list = (deptId != null) ? userRepository.findByDepartmentDepartmentId(deptId) : List.of();
        } else {
            // Employee can only view self
            User self = userRepository.findById(userDetails.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getUserId()));
            list = List.of(self);
        }

        return list.stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDTO> getUsersPaginated(
            Pageable pageable,
            String search,
            Long departmentId,
            UserStatus status,
            Long roleId,
            CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Specification<User> spec = UserSpecification.filterUsers(
                search,
                departmentId,
                status,
                roleId,
                userDetails.getRole(),
                userDetails.getDepartmentId(),
                userDetails.getUserId()
        );

        Page<User> page = userRepository.findAll(spec, pageable);
        Page<UserResponseDTO> dtoPage = page.map(userMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId, CustomUserDetails userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.ADMIN || role == RoleName.HR) {
            return userMapper.toResponse(user);
        }

        if (role == RoleName.MANAGER) {
            Long targetDeptId = user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null;
            if (targetDeptId == null || !targetDeptId.equals(userDetails.getDepartmentId())) {
                throw new AccessDeniedException("Access denied: Target user does not belong to your department");
            }
            return userMapper.toResponse(user);
        }

        // EMPLOYEE ownership check
        if (!user.getUserId().equals(userDetails.getUserId())) {
            throw new AccessDeniedException("Access denied: You can only view your own user profile");
        }

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponseDTO updateUser(Long userId, UserRequestDTO request, CustomUserDetails updater) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        // HR cannot elevate to or modify ADMIN accounts unless updater is ADMIN
        if ((role.getRoleName() == RoleName.ADMIN || user.getRole().getRoleName() == RoleName.ADMIN) &&
                (updater == null || updater.getRole() != RoleName.ADMIN)) {
            throw new AccessDeniedException("Access denied: HR cannot create, elevate, or modify ADMIN accounts");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId()));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setStatus(request.getStatus());
        user.setRole(role);
        user.setDepartment(department);
        user.setDesignation(designation);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long userId, CustomUserDetails deleter) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole().getRoleName() == RoleName.ADMIN && (deleter == null || deleter.getRole() != RoleName.ADMIN)) {
            throw new AccessDeniedException("Access denied: Cannot delete ADMIN users");
        }

        userRepository.delete(user);
    }
}