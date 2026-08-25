package com.hareeshvar.attendance.dto.response;

import java.time.LocalDateTime;

import com.hareeshvar.attendance.enums.Gender;
import com.hareeshvar.attendance.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long userId;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private Gender gender;

    private UserStatus status;

    private Long roleId;

    private String roleName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long departmentId;
    private String departmentName;

    private Long designationId;
    private String designationName;

}