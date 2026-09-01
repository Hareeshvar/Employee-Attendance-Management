package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hareeshvar.attendance.dto.request.NotificationRequestDTO;
import com.hareeshvar.attendance.dto.response.NotificationResponseDTO;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('NOTIFICATION_MANAGE', 'ROLE_ADMIN', 'ROLE_HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponseDTO createNotification(@Valid @RequestBody NotificationRequestDTO request) {
        return notificationService.createNotification(request);
    }

    @GetMapping
    public List<NotificationResponseDTO> getAllNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.getAllNotifications(userDetails);
    }

    @GetMapping("/{id}")
    public NotificationResponseDTO getNotificationById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.getNotificationById(id, userDetails);
    }

    @PutMapping("/{id}")
    public NotificationResponseDTO updateNotification(
            @PathVariable Long id,
            @RequestBody NotificationRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.updateNotification(id, request, userDetails);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('NOTIFICATION_MANAGE', 'ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }
}
