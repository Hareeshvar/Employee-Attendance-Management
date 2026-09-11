package com.hareeshvar.attendance.service;

import java.util.Optional;

import org.springframework.http.ResponseCookie;

import com.hareeshvar.attendance.entity.RefreshToken;
import com.hareeshvar.attendance.entity.User;

public interface RefreshTokenService {

    record TokenResult(String rawToken, RefreshToken refreshToken) {}

    TokenResult createRefreshToken(User user, String ipAddress, String userAgent);

    TokenResult rotateRefreshToken(String rawToken, String ipAddress, String userAgent);

    void revokeRefreshToken(String rawToken);

    void revokeAllUserTokens(Long userId);

    ResponseCookie createRefreshTokenCookie(String rawToken);

    ResponseCookie createCleanRefreshTokenCookie();
}
