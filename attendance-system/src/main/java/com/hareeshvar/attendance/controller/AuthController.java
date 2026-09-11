package com.hareeshvar.attendance.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hareeshvar.attendance.dto.auth.ChangePasswordRequest;
import com.hareeshvar.attendance.dto.auth.LoginRequest;
import com.hareeshvar.attendance.dto.auth.LoginResponse;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;
import com.hareeshvar.attendance.exception.BadRequestException;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.security.service.JwtService;
import com.hareeshvar.attendance.service.AuditLogService;
import com.hareeshvar.attendance.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("[AUTH DEBUG] Login request received for username/email: '{}'", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.info("[AUTH DEBUG] Authentication SUCCESSFUL for user: '{}', role: {}", userDetails.getUsername(), userDetails.getRole());

            User user = userRepository.findById(userDetails.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getUserId()));

            String accessToken = jwtService.generateAccessToken(userDetails);

            String ipAddress = servletRequest.getRemoteAddr();
            String userAgent = servletRequest.getHeader("User-Agent");
            RefreshTokenService.TokenResult refreshResult = refreshTokenService.createRefreshToken(user, ipAddress, userAgent);

            ResponseCookie refreshCookie = refreshTokenService.createRefreshTokenCookie(refreshResult.rawToken());

            auditLogService.logSecurityEvent(
                    AuditAction.LOGIN_SUCCESS,
                    userDetails.getUsername(),
                    userDetails.getRole() != null ? userDetails.getRole().name() : "EMPLOYEE",
                    "USER",
                    String.valueOf(userDetails.getUserId()),
                    "User logged in successfully",
                    AuditResult.SUCCESS,
                    Map.of("username", userDetails.getUsername())
            );

            LoginResponse response = LoginResponse.builder()
                    .token(accessToken)
                    .userId(userDetails.getUserId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .role(userDetails.getRole().name())
                    .departmentId(userDetails.getDepartmentId())
                    .departmentName(userDetails.getDepartmentName())
                    .designationName(userDetails.getDesignationName())
                    .permissions(userDetails.getPermissions())
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(response);

        } catch (BadCredentialsException e) {
            log.warn("[AUTH DEBUG] Password verification FAILED for input username/email: '{}'", request.getUsername());
            auditLogService.logSecurityEvent(
                    AuditAction.LOGIN_FAILED,
                    request.getUsername(),
                    "UNAUTHENTICATED",
                    "USER",
                    request.getUsername(),
                    "Failed login attempt for username/email: " + request.getUsername(),
                    AuditResult.FAILURE,
                    Map.of("attemptedUsername", request.getUsername())
            );
            throw e;
        } catch (Exception e) {
            log.error("[AUTH DEBUG] Authentication EXCEPTION for input username/email: '{}': {}", request.getUsername(), e.getMessage());
            auditLogService.logSecurityEvent(
                    AuditAction.LOGIN_FAILED,
                    request.getUsername(),
                    "UNAUTHENTICATED",
                    "USER",
                    request.getUsername(),
                    "Authentication exception: " + e.getMessage(),
                    AuditResult.FAILURE,
                    Map.of("attemptedUsername", request.getUsername())
            );
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletRequest servletRequest
    ) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new BadCredentialsException("Refresh token cookie is missing");
        }

        String ipAddress = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");

        RefreshTokenService.TokenResult rotatedResult = refreshTokenService.rotateRefreshToken(
                refreshTokenCookie, ipAddress, userAgent
        );

        User user = rotatedResult.refreshToken().getUser();
        CustomUserDetails userDetails = CustomUserDetails.create(user);
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        ResponseCookie newRefreshCookie = refreshTokenService.createRefreshTokenCookie(rotatedResult.rawToken());

        LoginResponse response = LoginResponse.builder()
                .token(newAccessToken)
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .role(userDetails.getRole().name())
                .departmentId(userDetails.getDepartmentId())
                .departmentName(userDetails.getDepartmentName())
                .designationName(userDetails.getDesignationName())
                .permissions(userDetails.getPermissions())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie
    ) {
        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            refreshTokenService.revokeRefreshToken(refreshTokenCookie);
        }

        ResponseCookie cleanCookie = refreshTokenService.createCleanRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails != null && userDetails.getUserId() != null) {
            refreshTokenService.revokeAllUserTokens(userDetails.getUserId());
            auditLogService.logSecurityEvent(
                    AuditAction.SESSION_REVOKED,
                    userDetails.getUsername(),
                    userDetails.getRole() != null ? userDetails.getRole().name() : "EMPLOYEE",
                    "USER",
                    String.valueOf(userDetails.getUserId()),
                    "All active user sessions revoked by user request",
                    AuditResult.SUCCESS,
                    Map.of("username", userDetails.getUsername())
            );
        }

        ResponseCookie cleanCookie = refreshTokenService.createCleanRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getUserId()));

        CustomUserDetails freshDetails = CustomUserDetails.create(user);

        LoginResponse response = LoginResponse.builder()
                .token(null)
                .userId(freshDetails.getUserId())
                .username(freshDetails.getUsername())
                .email(freshDetails.getEmail())
                .firstName(freshDetails.getFirstName())
                .lastName(freshDetails.getLastName())
                .role(freshDetails.getRole().name())
                .departmentId(freshDetails.getDepartmentId())
                .departmentName(freshDetails.getDepartmentName())
                .designationName(freshDetails.getDesignationName())
                .permissions(freshDetails.getPermissions())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getUserId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password verification failed");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Security requirement 27: revoke existing refresh sessions after password change
        refreshTokenService.revokeAllUserTokens(user.getUserId());

        auditLogService.logSuccess(
                AuditAction.USER_UPDATED,
                "USER",
                String.valueOf(user.getUserId()),
                "User changed password and invalidated active sessions",
                Map.of("username", user.getUsername())
        );

        ResponseCookie cleanCookie = refreshTokenService.createCleanRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }
}