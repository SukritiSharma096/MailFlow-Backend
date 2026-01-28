package com.mailProject.email.config;

import com.mailProject.email.entity.Admin;
import com.mailProject.email.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminDataLoader  implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (adminRepository.findByUsername("admin@admin.com").isEmpty()) {

            Admin admin = new Admin();
            admin.setUsername("admin@admin.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now());

            adminRepository.save(admin);

            System.out.println("✅ Default Admin Created");
        }
    }
}
