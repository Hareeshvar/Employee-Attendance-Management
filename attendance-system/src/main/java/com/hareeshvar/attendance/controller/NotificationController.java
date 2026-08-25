package com.hareeshvar.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
import com.hareeshvar.attendance.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponseDTO createNotification(
            @Valid @RequestBody NotificationRequestDTO request) {

        return notificationService.createNotification(request);
    }

    @GetMapping
    public List<NotificationResponseDTO> getAllNotifications() {

        return notificationService.getAllNotifications();
    }

    @GetMapping("/{id}")
    public NotificationResponseDTO getNotificationById(
            @PathVariable Long id) {

        return notificationService.getNotificationById(id);
    }

    @PutMapping("/{id}")
    public NotificationResponseDTO updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequestDTO request) {

        return notificationService.updateNotification(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable Long id) {

        notificationService.deleteNotification(id);
    }
}
