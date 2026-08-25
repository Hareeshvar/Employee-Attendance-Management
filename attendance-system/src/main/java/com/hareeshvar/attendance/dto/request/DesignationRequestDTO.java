package com.hareeshvar.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DesignationRequestDTO {

    @NotBlank
    private String designationName;

    private String description;

}