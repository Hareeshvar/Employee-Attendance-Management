package com.hareeshvar.attendance.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hareeshvar.attendance.security.filter.JwtAuthenticationFilter;
import com.hareeshvar.attendance.security.jwt.JwtAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(authenticationEntryPoint))

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // PUBLIC ENDPOINTS
                        // =========================

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/auth/login"
                        ).permitAll()

                        // Registration
                        // Change permitAll() to hasRole("ADMIN")
                        // after bootstrapping your project.
                        .requestMatchers(HttpMethod.POST, "/api/v1/users")
                        .permitAll()

                        // =========================
                        // ROLE MANAGEMENT
                        // =========================

                        .requestMatchers(HttpMethod.POST, "/api/v1/roles/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/roles/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/roles/**")
                        .hasRole("ADMIN")

                        // =========================
                        // DEPARTMENT MANAGEMENT
                        // =========================

                        .requestMatchers(HttpMethod.POST, "/api/v1/departments/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/departments/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/departments/**")
                        .hasRole("ADMIN")

                        // =========================
                        // USER MANAGEMENT
                        // =========================

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**")
                        .hasRole("ADMIN")

                        // =========================
                        // LEAVE APPROVAL
                        // =========================

                        .requestMatchers(HttpMethod.PUT, "/api/v1/leaves/*/approve")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/leaves/*/reject")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/designations/**")
.hasRole("ADMIN")

.requestMatchers(HttpMethod.PUT, "/api/v1/designations/**")
.hasRole("ADMIN")

.requestMatchers(HttpMethod.DELETE, "/api/v1/designations/**")
.hasRole("ADMIN")


.requestMatchers(HttpMethod.POST, "/api/v1/shifts/**")
.hasRole("ADMIN")

.requestMatchers(HttpMethod.PUT, "/api/v1/shifts/**")
.hasRole("ADMIN")

.requestMatchers(HttpMethod.DELETE, "/api/v1/shifts/**")
.hasRole("ADMIN")

.requestMatchers(HttpMethod.POST, "/api/v1/employee-shifts/**")
.hasRole("ADMIN")

.requestMatchers(HttpMethod.PUT, "/api/v1/employee-shifts/**")
.hasRole("ADMIN")

.requestMatchers(HttpMethod.DELETE, "/api/v1/employee-shifts/**")
.hasRole("ADMIN")

                        // =========================
                        // NOTIFICATION, PAYROLL & REPORT MANAGEMENT (ADMIN)
                        // =========================

                        .requestMatchers(HttpMethod.POST, "/api/v1/notifications/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/notifications/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/payrolls/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/payrolls/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/payrolls/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/reports/**")
                        .hasRole("ADMIN")

                        // =========================
                        // EMPLOYEE + ADMIN
                        // =========================

                        .requestMatchers(HttpMethod.POST, "/api/v1/leaves")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/leaves/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/v1/attendance/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/attendance/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/designations/**")
.hasAnyRole("ADMIN", "EMPLOYEE")

.requestMatchers(HttpMethod.GET, "/api/v1/shifts/**")
.hasAnyRole("ADMIN", "EMPLOYEE")

.requestMatchers(HttpMethod.GET, "/api/v1/employee-shifts/**")
.hasAnyRole("ADMIN", "EMPLOYEE")

                        // =========================
                        // NOTIFICATION & PAYROLL (EMPLOYEE + ADMIN)
                        // =========================

                        .requestMatchers(HttpMethod.PUT, "/api/v1/notifications/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/notifications/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/payrolls/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        // =========================
                        // EVERYTHING ELSE
                        // =========================

                        .anyRequest().authenticated()

                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}