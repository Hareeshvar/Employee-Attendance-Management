package com.hareeshvar.attendance.service;

import java.util.List;

import com.hareeshvar.attendance.dto.request.UserRequestDTO;
import com.hareeshvar.attendance.dto.response.UserResponseDTO;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO request);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long userId);

    UserResponseDTO updateUser(Long userId, UserRequestDTO request);

    void deleteUser(Long userId);

}