package com.hareeshvar.attendance.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.UserStatus;
import com.hareeshvar.attendance.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {

        log.info("[AUTH DEBUG] Searching database for user/email: '{}'", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    log.warn("[AUTH DEBUG] User NOT FOUND in database for input: '{}'", usernameOrEmail);
                    return new UsernameNotFoundException(
                            "User not found with username or email: " + usernameOrEmail);
                });

        log.info("[AUTH DEBUG] User FOUND: id={}, username='{}', email='{}', status={}, role={}",
                user.getUserId(), user.getUsername(), user.getEmail(), user.getStatus(),
                user.getRole() != null ? user.getRole().getRoleName() : "NULL");

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("[AUTH DEBUG] User account is NOT ACTIVE: status={}", user.getStatus());
            throw new UsernameNotFoundException("User account is inactive: " + usernameOrEmail);
        }

        return CustomUserDetails.create(user);
    }
}