package com.hareeshvar.attendance.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hareeshvar.attendance.dto.auth.LoginRequest;
import com.hareeshvar.attendance.dto.auth.LoginResponse;
import com.hareeshvar.attendance.security.service.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()));

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(user.getUsername());

        String role = user.getAuthorities()
                .stream()
                .findFirst()
                .get()
                .getAuthority();

        return new LoginResponse(
                token,
                user.getUsername(),
                role
        );
    }
}