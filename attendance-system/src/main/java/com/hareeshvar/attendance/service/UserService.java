package com.hareeshvar.attendance.service;

import java.util.List;
import org.springframework.data.domain.Pageable;

import com.hareeshvar.attendance.dto.request.UserRequestDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.dto.response.UserResponseDTO;
import com.hareeshvar.attendance.enums.UserStatus;
import com.hareeshvar.attendance.security.service.CustomUserDetails;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO request, CustomUserDetails creator);

    List<UserResponseDTO> getAllUsers(CustomUserDetails userDetails);

    PageResponse<UserResponseDTO> getUsersPaginated(
            Pageable pageable,
            String search,
            Long departmentId,
            UserStatus status,
            Long roleId,
            CustomUserDetails userDetails
    );

    UserResponseDTO getUserById(Long userId, CustomUserDetails userDetails);

    UserResponseDTO updateUser(Long userId, UserRequestDTO request, CustomUserDetails updater);

    void deleteUser(Long userId, CustomUserDetails deleter);
}