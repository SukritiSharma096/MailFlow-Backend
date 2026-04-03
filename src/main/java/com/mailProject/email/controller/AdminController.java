
package com.mailProject.email.controller;

import com.mailProject.email.dto.AdminLoginResponse;
import com.mailProject.email.dto.AdminRequest;
import com.mailProject.email.dto.AdminResponse;
import com.mailProject.email.repository.AdminRepository;
import com.mailProject.email.security.JwtUtil;
import com.mailProject.email.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(
            @Valid @RequestBody AdminRequest request,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String token = authHeader.substring(7);
        String loggedInRole  = jwtUtil.extractRoles(token).get(0);

        if ("ROLE_MANAGER".equals(loggedInRole) && !"USER".equalsIgnoreCase(request.getRole())) {
            return ResponseEntity.status(401).body("Manager can create only USER");
        }
        return ResponseEntity.ok(adminService.createAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminRequest request) {
        boolean valid = adminService.verifyAdmin(request.getUsername(), request.getPassword());
        if (!valid) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        AdminResponse admin = adminService.getAdminByUsername(request.getUsername());
        String token = jwtUtil.generateToken(
                admin.getUsername(),
                List.of("ROLE_" + admin.getRole())
        );

        return ResponseEntity.ok(
                new AdminLoginResponse(token, "Bearer", admin.getUsername(), admin.getRole())
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest httpRequest) {
        String username = jwtUtil.extractUsernameFromRequest(httpRequest);
        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok(adminService.getAdminByUsername(username));
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllAdmins(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getAdminById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.extractRoles(token).get(0);

        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_MANAGER")) {
            return ResponseEntity.status(401).body("Access denied");
        }

        AdminResponse admin = adminService.getAdminById(id);
        if (admin == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        return ResponseEntity.ok(admin);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAdmin(
            @PathVariable Long id,
            @RequestBody AdminRequest request,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.extractRoles(token).get(0);
        String username = jwtUtil.extractUsername(token);

        return ResponseEntity.ok(adminService.updateAdmin(id, request, username, role));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id, HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.extractRoles(token).get(0);

        if (!role.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(403).body("Only ADMIN can delete users");
        }
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
