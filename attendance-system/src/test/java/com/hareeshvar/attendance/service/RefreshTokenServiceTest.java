package com.hareeshvar.attendance.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import com.hareeshvar.attendance.entity.RefreshToken;
import com.hareeshvar.attendance.entity.Role;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.enums.UserStatus;
import com.hareeshvar.attendance.repository.RefreshTokenRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.service.impl.RefreshTokenServiceImpl;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User sampleUser;
    private RefreshToken sampleRefreshToken;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().roleId(1L).roleName(RoleName.EMPLOYEE).build();
        sampleUser = User.builder()
                .userId(42L)
                .username("test_user")
                .status(UserStatus.ACTIVE)
                .role(role)
                .build();

        sampleRefreshToken = RefreshToken.builder()
                .id(100L)
                .user(sampleUser)
                .tokenHash("samplehash123")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
    }

    @Test
    void testCreateRefreshToken_Success() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(sampleRefreshToken);

        RefreshTokenService.TokenResult result = refreshTokenService.createRefreshToken(sampleUser, "127.0.0.1", "Mozilla/5.0");

        assertNotNull(result);
        assertNotNull(result.rawToken());
        assertEquals(sampleRefreshToken, result.refreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testRotateRefreshToken_ReuseDetection_ShouldRevokeAllSessions() {
        RefreshToken revokedToken = RefreshToken.builder()
                .id(101L)
                .user(sampleUser)
                .tokenHash("revokedhash")
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(6))
                .revoked(true) // Revoked token presented again!
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(revokedToken));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                refreshTokenService.rotateRefreshToken("dummyRawToken", "127.0.0.1", "Mozilla/5.0")
        );

        assertTrue(exception.getMessage().contains("reuse detected"));
        verify(refreshTokenRepository).revokeAllUserTokens(eq(sampleUser.getUserId()));
    }

    @Test
    void testRotateRefreshToken_DisabledUser_ShouldFail() {
        User disabledUser = User.builder()
                .userId(42L)
                .username("test_user")
                .status(UserStatus.INACTIVE)
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(sampleRefreshToken));
        when(userRepository.findById(eq(42L))).thenReturn(Optional.of(disabledUser));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                refreshTokenService.rotateRefreshToken("dummyRawToken", "127.0.0.1", "Mozilla/5.0")
        );

        assertTrue(exception.getMessage().contains("disabled or inactive"));
    }

    @Test
    void testRotateRefreshToken_ConcurrentRaceCondition_ShouldFail() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(sampleRefreshToken));
        when(userRepository.findById(eq(42L))).thenReturn(Optional.of(sampleUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(sampleRefreshToken);
        // Simulate race condition where row count updated is 0
        when(refreshTokenRepository.revokeTokenAtomic(eq(100L), any())).thenReturn(0);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                refreshTokenService.rotateRefreshToken("dummyRawToken", "127.0.0.1", "Mozilla/5.0")
        );

        assertTrue(exception.getMessage().contains("race condition"));
    }
}
