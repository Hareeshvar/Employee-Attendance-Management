package com.hareeshvar.attendance.security.config;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hareeshvar.attendance.exception.ErrorCode;
import com.hareeshvar.attendance.exception.ErrorResponse;
import com.hareeshvar.attendance.security.filter.JwtAuthenticationFilter;
import com.hareeshvar.attendance.security.filter.RequestCorrelationFilter;
import com.hareeshvar.attendance.security.jwt.JwtAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RequestCorrelationFilter requestCorrelationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    private final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return om;
    }

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
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String requestId = (String) request.getAttribute(RequestCorrelationFilter.ATTRIBUTE_REQUEST_ID);
                            if (requestId == null) {
                                requestId = MDC.get(RequestCorrelationFilter.MDC_REQUEST_ID_KEY);
                            }

                            ErrorResponse errorResponse = ErrorResponse.builder()
                                    .success(false)
                                    .timestamp(LocalDateTime.now())
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                                    .errorCode(ErrorCode.ACCESS_DENIED)
                                    .message("Access Denied: You do not have permission to perform this operation or access this resource")
                                    .path(request.getRequestURI())
                                    .requestId(requestId)
                                    .build();

                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                )
                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // PUBLIC ENDPOINTS
                        // =========================
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        ).permitAll()

                        // =========================
                        // AUTH / ME & PROFILE
                        // =========================
                        .requestMatchers("/api/v1/auth/me", "/api/v1/auth/change-password").authenticated()

                        // =========================
                        // USER MANAGEMENT (ADMIN, HR, MANAGER)
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/users", "/api/v1/users/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users", "/api/v1/users/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users", "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users", "/api/v1/users/**").hasAnyRole("ADMIN", "HR", "MANAGER")

                        // =========================
                        // ROLE MANAGEMENT
                        // =========================
                        .requestMatchers(HttpMethod.GET, "/api/v1/roles", "/api/v1/roles/**").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .requestMatchers("/api/v1/roles", "/api/v1/roles/**").hasRole("ADMIN")

                        // =========================
                        // DEPARTMENT & DESIGNATION MANAGEMENT
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/departments", "/api/v1/departments/**", "/api/v1/designations", "/api/v1/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/departments", "/api/v1/departments/**", "/api/v1/designations", "/api/v1/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/departments", "/api/v1/departments/**", "/api/v1/designations", "/api/v1/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/departments", "/api/v1/departments/**", "/api/v1/designations", "/api/v1/designations/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // =========================
                        // SHIFTS & EMPLOYEE SHIFTS
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/shifts", "/api/v1/shifts/**", "/api/v1/employee-shifts", "/api/v1/employee-shifts/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shifts", "/api/v1/shifts/**", "/api/v1/employee-shifts", "/api/v1/employee-shifts/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/shifts", "/api/v1/shifts/**", "/api/v1/employee-shifts", "/api/v1/employee-shifts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/shifts", "/api/v1/shifts/**", "/api/v1/employee-shifts", "/api/v1/employee-shifts/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // =========================
                        // LEAVES
                        // =========================
                        .requestMatchers(HttpMethod.PUT, "/api/v1/leaves/*/approve", "/api/v1/leaves/*/reject").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/leaves", "/api/v1/leaves/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/leaves", "/api/v1/leaves/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/leaves", "/api/v1/leaves/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                        // =========================
                        // ATTENDANCE
                        // =========================
                        .requestMatchers("/api/v1/attendance", "/api/v1/attendance/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // =========================
                        // PAYROLL
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/payrolls", "/api/v1/payrolls/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/payrolls", "/api/v1/payrolls/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/payrolls", "/api/v1/payrolls/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/payrolls", "/api/v1/payrolls/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                        // =========================
                        // REPORTS
                        // =========================
                        .requestMatchers("/api/v1/reports", "/api/v1/reports/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // =========================
                        // NOTIFICATIONS
                        // =========================
                        .requestMatchers(HttpMethod.POST, "/api/v1/notifications", "/api/v1/notifications/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/notifications", "/api/v1/notifications/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/notifications", "/api/v1/notifications/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // =========================
                        // AUDIT LOGS (ADMIN, HR)
                        // =========================
                        .requestMatchers("/api/v1/audit-logs", "/api/v1/audit-logs/**").hasAnyRole("ADMIN", "HR")

                        // =========================
                        // EVERYTHING ELSE
                        // =========================
                        .anyRequest().authenticated()
                )
                .addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "X-Request-ID"));
        configuration.setExposedHeaders(Arrays.asList("X-Request-ID"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}