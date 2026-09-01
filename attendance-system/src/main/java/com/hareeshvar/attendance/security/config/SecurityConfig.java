package com.hareeshvar.attendance.security.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hareeshvar.attendance.security.filter.JwtAuthenticationFilter;
import com.hareeshvar.attendance.security.jwt.JwtAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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

                        // =========================
                        // AUTH / ME & PROFILE
                        // =========================
                        .requestMatchers("/api/v1/auth/me", "/api/v1/auth/change-password").authenticated()

                        // =========================
                        // USER MANAGEMENT (ADMIN, HR)
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("ADMIN", "HR", "MANAGER")

                        // =========================
                        // ROLE MANAGEMENT (ADMIN ONLY)
                        // =========================
                        .requestMatchers("/api/v1/roles/**").hasRole("ADMIN")

                        // =========================
                        // DEPARTMENT & DESIGNATION MANAGEMENT
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/departments/**", "/api/v1/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/departments/**", "/api/v1/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/departments/**", "/api/v1/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/departments/**", "/api/v1/designations/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // =========================
                        // SHIFTS & EMPLOYEE SHIFTS
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/shifts/**", "/api/v1/employee-shifts/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shifts/**", "/api/v1/employee-shifts/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/shifts/**", "/api/v1/employee-shifts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/shifts/**", "/api/v1/employee-shifts/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // =========================
                        // LEAVES
                        // =========================
                        .requestMatchers(HttpMethod.PUT, "/api/v1/leaves/*/approve", "/api/v1/leaves/*/reject").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/leaves").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/leaves/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/leaves/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                        // =========================
                        // ATTENDANCE
                        // =========================
                        .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // =========================
                        // PAYROLL
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/payrolls/**", "/api/v1/payrolls").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/payrolls/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/payrolls/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/payrolls/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                        // =========================
                        // REPORTS
                        // =========================
                        .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // =========================
                        // NOTIFICATIONS
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/notifications/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/notifications/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/notifications/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}