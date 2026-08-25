package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.NotificationRequestDTO;
import com.hareeshvar.attendance.dto.response.NotificationResponseDTO;

public interface NotificationService {

    NotificationResponseDTO createNotification(NotificationRequestDTO request);

    List<NotificationResponseDTO> getAllNotifications();

    NotificationResponseDTO getNotificationById(Long notificationId);

    NotificationResponseDTO updateNotification(Long notificationId, NotificationRequestDTO request);

    void deleteNotification(Long notificationId);

}
