package com.hareeshvar.attendance.security.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.Permission;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final RoleName role;
    private final Long departmentId;
    private final String departmentName;
    private final String designationName;
    private final UserStatus status;
    private final Set<GrantedAuthority> authorities;
    private final Set<String> permissions;

    public static CustomUserDetails create(User user) {
        RoleName roleName = user.getRole() != null ? user.getRole().getRoleName() : RoleName.EMPLOYEE;
        Set<Permission> perms = Permission.getPermissionsForRole(roleName);

        Set<GrantedAuthority> authorities = new HashSet<>();
        // Add ROLE_ authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.name()));

        // Add permission authorities
        for (Permission perm : perms) {
            authorities.add(new SimpleGrantedAuthority(perm.name()));
        }

        Set<String> permissionStrings = perms.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        Long deptId = user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null;
        String deptName = user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null;
        String desigName = user.getDesignation() != null ? user.getDesignation().getDesignationName() : null;

        return CustomUserDetails.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(roleName)
                .departmentId(deptId)
                .departmentName(deptName)
                .designationName(desigName)
                .status(user.getStatus())
                .authorities(authorities)
                .permissions(permissionStrings)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
