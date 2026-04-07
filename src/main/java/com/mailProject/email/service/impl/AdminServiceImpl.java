package com.mailProject.email.service.impl;

import com.mailProject.email.dto.AdminRequest;
import com.mailProject.email.dto.AdminResponse;
import com.mailProject.email.entity.Admin;
import com.mailProject.email.repository.AdminRepository;
import com.mailProject.email.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AdminResponse createAdmin(AdminRequest request) {
        if (adminRepository.findByUsername(request.getUsername()).isPresent())
            throw new RuntimeException("User already exists");

        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getRole() != null && !request.getRole().isBlank()) {
            admin.setRole(request.getRole().toUpperCase());
        } else {
            admin.setRole("USER");
        }
        admin.setCreatedAt(LocalDateTime.now());
        admin.setActive(true);

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

    @Override
    public List<AdminResponse> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        List<AdminResponse> responses = new ArrayList<>();

        for (Admin admin : admins) {
            AdminResponse resp = new AdminResponse();
            resp.setId(admin.getId());
            resp.setUsername(admin.getUsername());
            resp.setRole(admin.getRole());
            responses.add(resp);
        }

        return responses;
    }
    @Override
    public AdminResponse updateAdmin(Long id, AdminRequest request, String loggedInUsername, String role) {
        if ("USER".equals(role)) {
            throw new RuntimeException("USER cannot update anyone");
        }

        Admin target = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("MANAGER".equals(role)) {

            if ("ADMIN".equals(target.getRole())) {
                throw new RuntimeException("Manager cannot update admin profile");
            }

            boolean isSelf = loggedInUsername.equals(target.getUsername());
            boolean isUser = "USER".equals(target.getRole());

            if (!isSelf && !isUser) {
                throw new RuntimeException("Manager can update only self or USER");
            }
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(target.getUsername())) {
            if (adminRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            target.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            target.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            if ("MANAGER".equals(role) && !"USER".equals(target.getRole())) {
                throw new RuntimeException("Manager cannot change role of self or other MANAGER");
            }
            target.setRole(request.getRole());
        }

        Admin updated = adminRepository.save(target);
        return toResponse(updated);
    }
    @Override
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Admin with id " + id + " not found"));

        adminRepository.delete(admin);
    }
    @Override
    public AdminResponse getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(admin);
    }
    private AdminResponse toResponse(Admin admin) {
        AdminResponse resp = new AdminResponse();
        resp.setId(admin.getId());
        resp.setUsername(admin.getUsername());
        resp.setRole(admin.getRole());
        resp.setActive(admin.getActive());
        resp.setCreatedAt(admin.getCreatedAt());
        return resp;
    }

}
