package com.mailProject.email.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class SchedulerRequestDto {

    @NotBlank
    private String name;
    @NotBlank
    private String cronExpression;
    private String description;
}