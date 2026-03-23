package com.mailProject.email.service;

import com.mailProject.email.entity.Admin;
import com.mailProject.email.entity.ClickupConfig;
import com.mailProject.email.repository.AdminRepository;
import com.mailProject.email.repository.ClickupConfigRepository;
import com.mailProject.email.security.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClickupConfigService {

    private final ClickupConfigRepository configRepo;
    private final AdminRepository adminRepo;

    private Admin getCurrentAdmin() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return adminRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }


    public void saveOrUpdate(String token, String listId) {

        Admin admin = getCurrentAdmin();

        ClickupConfig config = configRepo
                .findByAdmin(admin)
                .orElse(new ClickupConfig());

        config.setAdmin(admin);
        config.setToken(AESUtil.encrypt(token));
        config.setListId(listId);

        configRepo.save(config);
    }

    public boolean isConfigured() {
        Admin admin = getCurrentAdmin();
        return configRepo.findByAdmin(admin).isPresent();
    }

    public ClickupConfig getConfig() {
        Admin admin = getCurrentAdmin();
        return configRepo.findByAdmin(admin)
                .orElseThrow(() -> new RuntimeException("ClickUp not configured"));
    }

    public String getDecryptedToken() {
        ClickupConfig config = getConfig();
        return AESUtil.decrypt(config.getToken());
    }
}
