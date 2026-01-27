package com.mailProject.email.dto;

import lombok.Data;

@Data
public class TaskResponse {
    private String id;
    private String name;
    private String description;
    private Status status;
}
