package com.hareeshvar.attendance.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.hareeshvar.attendance.entity.RefreshToken;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;
import com.hareeshvar.attendance.enums.UserStatus;
import com.hareeshvar.attendance.repository.RefreshTokenRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.service.AuditLogService;
import com.hareeshvar.attendance.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final String COOKIE_NAME = "refreshToken";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Value("${app.jwt.refresh-token-expiration-ms:604800000}") // 7 days
    private long refreshTokenExpirationMs;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.cookie.path:/api/v1/auth}")
    private String cookiePath;

    @Override
    @Transactional
    public TokenResult createRefreshToken(User user, String ipAddress, String userAgent) {
        String rawToken = generateSecureTokenString();
        String tokenHash = hashToken(rawToken);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusNanos(refreshTokenExpirationMs * 1_000_000);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .createdAt(now)
                .expiresAt(expiresAt)
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        return new TokenResult(rawToken, saved);
    }

    @Override
    @Transactional
    public TokenResult rotateRefreshToken(String rawToken, String ipAddress, String userAgent) {
        if (!StringUtils.hasText(rawToken)) {
            throw new BadCredentialsException("Refresh token cookie is missing");
        }

        String tokenHash = hashToken(rawToken);
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByTokenHash(tokenHash);

        if (optionalToken.isEmpty()) {
            log.warn("Invalid refresh token hash provided");
            throw new BadCredentialsException("Invalid refresh token");
        }

        RefreshToken existingToken = optionalToken.get();
        User user = existingToken.getUser();

        // Check 1: Token Reuse Detection
        if (existingToken.isRevoked()) {
            log.warn("TOKEN REUSE DETECTED! Revoking all sessions for user: '{}'", user.getUsername());
            refreshTokenRepository.revokeAllUserTokens(user.getUserId());

            auditLogService.logSecurityEvent(
                    AuditAction.TOKEN_REUSE_DETECTED,
                    user.getUsername(),
                    user.getRole() != null ? user.getRole().getRoleName().name() : "EMPLOYEE",
                    "USER",
                    String.valueOf(user.getUserId()),
                    "Revoked refresh token reuse detected. All user sessions invalidated.",
                    AuditResult.DENIED,
                    Map.of("username", user.getUsername())
            );

            throw new BadCredentialsException("Refresh token reuse detected. Session invalidated.");
        }

        // Check 2: Expiration Check
        if (existingToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);
            throw new BadCredentialsException("Refresh token expired");
        }

        // Check 3: Disabled / Inactive User Check
        User freshUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new BadCredentialsException("User account not found"));

        if (freshUser.getStatus() != UserStatus.ACTIVE) {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);
            throw new BadCredentialsException("User account is disabled or inactive");
        }

        // Generate new token BEFORE atomic revocation
        String newRawToken = generateSecureTokenString();
        String newTokenHash = hashToken(newRawToken);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpiresAt = now.plusNanos(refreshTokenExpirationMs * 1_000_000);

        RefreshToken newToken = RefreshToken.builder()
                .user(freshUser)
                .tokenHash(newTokenHash)
                .createdAt(now)
                .expiresAt(newExpiresAt)
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        RefreshToken savedNewToken = refreshTokenRepository.save(newToken);

        // Atomic conditional update to prevent race conditions during concurrent refresh requests
        int rowsUpdated = refreshTokenRepository.revokeTokenAtomic(existingToken.getId(), savedNewToken.getId());
        if (rowsUpdated == 0) {
            // Concurrent request already rotated this token!
            log.warn("Concurrent refresh race condition detected for user: '{}'", freshUser.getUsername());
            throw new BadCredentialsException("Refresh token race condition detected");
        }

        auditLogService.logSecurityEvent(
                AuditAction.TOKEN_REFRESH,
                freshUser.getUsername(),
                freshUser.getRole() != null ? freshUser.getRole().getRoleName().name() : "EMPLOYEE",
                "USER",
                String.valueOf(freshUser.getUserId()),
                "User successfully refreshed authentication session",
                AuditResult.SUCCESS,
                Map.of("username", freshUser.getUsername())
        );

        return new TokenResult(newRawToken, savedNewToken);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return;
        }
        try {
            String tokenHash = hashToken(rawToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                refreshTokenRepository.revokeTokenAtomic(token.getId(), null);
                auditLogService.logSecurityEvent(
                        AuditAction.LOGOUT,
                        token.getUser().getUsername(),
                        token.getUser().getRole() != null ? token.getUser().getRole().getRoleName().name() : "EMPLOYEE",
                        "USER",
                        String.valueOf(token.getUser().getUserId()),
                        "User logged out and session was revoked",
                        AuditResult.SUCCESS,
                        Map.of("username", token.getUser().getUsername())
                );
            });
        } catch (Exception e) {
            log.warn("Error revoking refresh token", e);
        }
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    @Override
    public ResponseCookie createRefreshTokenCookie(String rawToken) {
        return ResponseCookie.from(COOKIE_NAME, rawToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(cookiePath)
                .maxAge(refreshTokenExpirationMs / 1000)
                .build();
    }

    @Override
    public ResponseCookie createCleanRefreshTokenCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(cookiePath)
                .maxAge(0)
                .build();
    }

    private String generateSecureTokenString() {
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
