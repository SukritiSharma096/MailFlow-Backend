package com.mailProject.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminLoginResponse {

    private String token;
    private String tokenType;
    private String username;
    private String role;
}

