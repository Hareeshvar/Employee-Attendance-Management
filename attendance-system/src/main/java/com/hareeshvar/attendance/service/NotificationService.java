package com.hareeshvar.attendance.service;

import java.util.List;
import org.springframework.data.domain.Pageable;

import com.hareeshvar.attendance.dto.request.NotificationRequestDTO;
import com.hareeshvar.attendance.dto.response.NotificationResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.security.service.CustomUserDetails;

public interface NotificationService {

    NotificationResponseDTO createNotification(NotificationRequestDTO request);

    List<NotificationResponseDTO> getAllNotifications(CustomUserDetails userDetails);

    PageResponse<NotificationResponseDTO> getNotificationsPaginated(
            Pageable pageable,
            String search,
            Boolean isRead,
            CustomUserDetails userDetails
    );

    NotificationResponseDTO getNotificationById(Long notificationId, CustomUserDetails userDetails);

    NotificationResponseDTO updateNotification(Long notificationId, NotificationRequestDTO request, CustomUserDetails userDetails);

    void deleteNotification(Long notificationId);
}
