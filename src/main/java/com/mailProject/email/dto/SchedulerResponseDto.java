package com.mailProject.email.dto;

import lombok.Data;

@Data
public class SchedulerResponseDto {

    private Long id;
    private String name;
    private String cronExpression;
    private String description;
    private Boolean status;
}
