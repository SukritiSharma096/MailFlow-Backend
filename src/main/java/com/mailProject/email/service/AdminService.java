package com.mailProject.email.service;

import com.mailProject.email.dto.AdminRequest;
import com.mailProject.email.dto.AdminResponse;
import java.util.List;

public interface AdminService {

    AdminResponse createAdmin(AdminRequest request);

    boolean verifyAdmin(String username, String password);

    AdminResponse getAdminByUsername(String username);
    List<AdminResponse> getAllAdmins();
    AdminResponse updateAdmin(AdminRequest request);
    void deleteAdmin(Long id);
}
