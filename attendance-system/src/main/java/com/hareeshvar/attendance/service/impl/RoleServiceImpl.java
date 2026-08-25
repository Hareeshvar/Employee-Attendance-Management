package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hareeshvar.attendance.dto.request.RoleRequestDTO;
import com.hareeshvar.attendance.dto.response.RoleResponseDTO;
import com.hareeshvar.attendance.entity.Role;
import com.hareeshvar.attendance.exception.ResourceAlreadyExistsException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.RoleMapper;
import com.hareeshvar.attendance.repository.RoleRepository;
import com.hareeshvar.attendance.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO request) {

        if (roleRepository.existsByRoleName(request.getRoleName())) {
            throw new ResourceAlreadyExistsException(
                    "Role already exists: " + request.getRoleName());
        }

        Role role = roleMapper.toEntity(request);

        Role savedRole = roleRepository.save(role);

        return roleMapper.toResponse(savedRole);
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    public RoleResponseDTO getRoleById(Long roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found with id : " + roleId));

        return roleMapper.toResponse(role);
    }

    @Override
public RoleResponseDTO updateRole(Long roleId, RoleRequestDTO request) {

    Role role = roleRepository.findById(roleId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Role not found with id : " + roleId));

    roleRepository.findByRoleName(request.getRoleName())
            .ifPresent(existingRole -> {
                if (!existingRole.getRoleId().equals(roleId)) {
                    throw new ResourceAlreadyExistsException(
                            "Role already exists: " + request.getRoleName());
                }
            });

    role.setRoleName(request.getRoleName());
    role.setDescription(request.getDescription());

    Role updatedRole = roleRepository.save(role);

    return roleMapper.toResponse(updatedRole);
}

    @Override
    public void deleteRole(Long roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found with id : " + roleId));

        roleRepository.delete(role);
    }
}