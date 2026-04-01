package com.mailProject.email.dto;

import lombok.Data;

@Data
public class AdminRequest {

    private String username;
    private String password;
    private String role;
}
