package com.hareeshvar.attendance.security.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms:900000}") // Default: 15 minutes
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userDetails.getUserId());
        extraClaims.put("role", userDetails.getRole().name());
        extraClaims.put("email", userDetails.getEmail());
        extraClaims.put("type", "ACCESS");
        if (userDetails.getDepartmentId() != null) {
            extraClaims.put("departmentId", userDetails.getDepartmentId());
        }

        return generateToken(extraClaims, userDetails.getUsername(), jwtExpiration);
    }

    public String generateToken(CustomUserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, String username, long expirationMs) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }

    public String extractRole(String token) {
        Claims claims = extractClaims(token);
        return (String) claims.get("role");
    }

    public String extractTokenType(String token) {
        Claims claims = extractClaims(token);
        return (String) claims.get("type");
    }

    public boolean isTokenValid(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            final String tokenType = extractTokenType(token);
            return (tokenUsername != null &&
                    tokenUsername.equals(username) &&
                    "ACCESS".equals(tokenType) &&
                    !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}