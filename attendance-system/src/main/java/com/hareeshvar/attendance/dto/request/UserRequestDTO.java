package com.hareeshvar.attendance.dto.request;

import com.hareeshvar.attendance.enums.Gender;
import com.hareeshvar.attendance.enums.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UserRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(max = 50)
    private String username;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull
    private Gender gender;

    @NotNull
    private UserStatus status;

    @NotNull
    private Long roleId;

    private Long departmentId;

    @NotNull(message = "Designation is required")
    private Long designationId;

}