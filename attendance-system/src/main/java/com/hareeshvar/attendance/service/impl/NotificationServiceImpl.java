package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hareeshvar.attendance.dto.request.NotificationRequestDTO;
import com.hareeshvar.attendance.dto.response.NotificationResponseDTO;
import com.hareeshvar.attendance.entity.Notification;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.NotificationMapper;
import com.hareeshvar.attendance.repository.NotificationRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponseDTO createNotification(NotificationRequestDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + request.getUserId()));

        Notification notification = notificationMapper.toEntity(request);
        notification.setUser(user);

        Notification savedNotification = notificationRepository.save(notification);

        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    public List<NotificationResponseDTO> getAllNotifications() {

        return notificationRepository.findAll()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponseDTO getNotificationById(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with id : " + notificationId));

        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponseDTO updateNotification(Long notificationId,
                                                      NotificationRequestDTO request) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with id : " + notificationId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + request.getUserId()));

        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        if (request.getIsRead() != null) {
            notification.setIsRead(request.getIsRead());
        }

        Notification updatedNotification = notificationRepository.save(notification);

        return notificationMapper.toResponse(updatedNotification);
    }

    @Override
    public void deleteNotification(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with id : " + notificationId));

        notificationRepository.delete(notification);
    }
}
