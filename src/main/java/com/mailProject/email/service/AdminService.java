package com.mailProject.email.service;

import com.mailProject.email.dto.AdminRequest;
import com.mailProject.email.dto.AdminResponse;

public interface AdminService {

    AdminResponse createAdmin(AdminRequest request);

    boolean verifyAdmin(String username, String password);

    AdminResponse getAdminByUsername(String username);
}
