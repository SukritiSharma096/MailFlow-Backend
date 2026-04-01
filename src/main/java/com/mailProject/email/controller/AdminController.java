package com.mailProject.email.controller;

import com.mailProject.email.dto.AdminLoginResponse;
import com.mailProject.email.dto.AdminRequest;
import com.mailProject.email.dto.AdminResponse;
import com.mailProject.email.security.JwtUtil;
import com.mailProject.email.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<AdminResponse> createAdmin(@Valid
            @RequestBody AdminRequest request) {
        return ResponseEntity.ok(adminService.createAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminRequest request) {

        boolean valid = adminService.verifyAdmin(
                request.getUsername(),
                request.getPassword());
        if (!valid) {
            return ResponseEntity
                    .status(401)
                    .body("Invalid username or password");
        }

        AdminResponse admin =
                adminService.getAdminByUsername(request.getUsername());
        String token = jwtUtil.generateToken(
                admin.getUsername(),
                List.of("ROLE_" + admin.getRole())
        );

        return ResponseEntity.ok(
                new AdminLoginResponse(
                        token,
                        "Bearer",
                        admin.getUsername(),
                        admin.getRole()
                )
        );
    }

    @GetMapping("/read/{username}")
    public ResponseEntity<AdminResponse> getAdmin(
            @PathVariable String username) {
        return ResponseEntity.ok(
                adminService.getAdminByUsername(username)
        );
    }
    @GetMapping("/all")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        List<AdminResponse> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AdminResponse> updateAdmin(@RequestBody AdminRequest request) {
        return ResponseEntity.ok(adminService.updateAdmin(request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok("Admin deleted successfully");
    }
}
