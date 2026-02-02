package com.mailProject.email.service.impl;

import com.mailProject.email.dto.AdminRequest;
import com.mailProject.email.dto.AdminResponse;
import com.mailProject.email.entity.Admin;
import com.mailProject.email.repository.AdminRepository;
import com.mailProject.email.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AdminResponse toResponse(Admin admin) {
        AdminResponse resp = new AdminResponse();
        resp.setId(admin.getId());
        resp.setUsername(admin.getUsername());
        resp.setRole(admin.getRole());
        resp.setActive(admin.getActive());
        resp.setCreatedAt(admin.getCreatedAt());
        return resp;
    }

    @Override
    public AdminResponse createAdmin(AdminRequest request) {
        if(adminRepository.findByUsername(request.getUsername()).isPresent())
            throw new RuntimeException("Admin already exists");

        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole("ADMIN");

        Admin saved = adminRepository.save(admin);
        return toResponse(saved);
    }

    @Override
    public boolean verifyAdmin(String username, String password) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return passwordEncoder.matches(password, admin.getPassword());
    }

    @Override
    public AdminResponse getAdminByUsername(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        return toResponse(admin);
    }
}
