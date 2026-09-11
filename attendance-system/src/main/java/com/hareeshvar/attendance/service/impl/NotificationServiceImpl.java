package com.hareeshvar.attendance.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.dto.request.NotificationRequestDTO;
import com.hareeshvar.attendance.dto.response.NotificationResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.entity.Notification;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.enums.RoleName;
import com.hareeshvar.attendance.exception.ResourceNotFoundException;
import com.hareeshvar.attendance.mapper.NotificationMapper;
import com.hareeshvar.attendance.repository.NotificationRepository;
import com.hareeshvar.attendance.repository.UserRepository;
import com.hareeshvar.attendance.repository.specification.NotificationSpecification;
import com.hareeshvar.attendance.security.service.CustomUserDetails;
import com.hareeshvar.attendance.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponseDTO createNotification(NotificationRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Notification notification = notificationMapper.toEntity(request);
        notification.setUser(user);

        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getAllNotifications(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.ADMIN || role == RoleName.HR) {
            return notificationRepository.findAll().stream().map(notificationMapper::toResponse).toList();
        }

        return notificationRepository.findByUserUserId(userDetails.getUserId())
                .stream().map(notificationMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponseDTO> getNotificationsPaginated(
            Pageable pageable,
            String search,
            Boolean isRead,
            CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Specification<Notification> spec = NotificationSpecification.filterNotifications(
                search,
                isRead,
                userDetails.getRole(),
                userDetails.getUserId()
        );

        Page<Notification> page = notificationRepository.findAll(spec, pageable);
        Page<NotificationResponseDTO> dtoPage = page.map(notificationMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDTO getNotificationById(Long notificationId, CustomUserDetails userDetails) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        RoleName role = userDetails.getRole();
        if (role == RoleName.ADMIN || role == RoleName.HR) {
            return notificationMapper.toResponse(notification);
        }

        if (!notification.getUser().getUserId().equals(userDetails.getUserId())) {
            throw new AccessDeniedException("Access denied: You can only view your own notifications");
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponseDTO updateNotification(Long notificationId, NotificationRequestDTO request, CustomUserDetails userDetails) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required");
        }

        // Allow user to mark their own notification read
        if (userDetails.getRole() != RoleName.ADMIN && userDetails.getRole() != RoleName.HR) {
            if (!notification.getUser().getUserId().equals(userDetails.getUserId())) {
                throw new AccessDeniedException("Access denied: You can only update your own notifications");
            }
        }

        if (request.getIsRead() != null) {
            notification.setIsRead(request.getIsRead());
        }

        if (userDetails.getRole() == RoleName.ADMIN || userDetails.getRole() == RoleName.HR) {
            if (request.getTitle() != null) notification.setTitle(request.getTitle());
            if (request.getMessage() != null) notification.setMessage(request.getMessage());
        }

        Notification updatedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(updatedNotification);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notificationRepository.delete(notification);
    }
}
