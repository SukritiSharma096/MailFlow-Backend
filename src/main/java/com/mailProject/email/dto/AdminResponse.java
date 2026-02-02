package com.mailProject.email.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminResponse {

    private Long id;
    private String username;
    private String role;
    private Boolean active;
    private LocalDateTime createdAt;
}
