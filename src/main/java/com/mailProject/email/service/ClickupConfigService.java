package com.mailProject.email.service;

import com.mailProject.email.entity.ClickupConfig;
import com.mailProject.email.feignInterface.ClickupClient;
import com.mailProject.email.repository.ClickupConfigRepository;
import com.mailProject.email.security.AESUtil;
import com.mailProject.email.security.ClickupContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClickupConfigService {

    private final ClickupClient clickupClient;
    private final ClickupConfigRepository repo;

    public ClickupConfig getConfig() {
        return repo.findTopByOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("ClickUp config not found"));
    }

    public void save(String token, String teamId) {
        ClickupConfig config = new ClickupConfig();
        config.setToken(AESUtil.encrypt(token));
        config.setTeamId(teamId);
        repo.save(config);
    }

    public boolean isConfigured() {
        try {
            ClickupConfig c = getConfig();
            return c.getToken() != null
                    && c.getTeamId() != null
                    && c.getListId() != null; // final required
        } catch (Exception e) {
            return false;
        }
    }

    public void saveConfig(ClickupConfig config) {
        repo.save(config);
    }

    public Object getSpaces() {
        ClickupConfig config = getConfig();
        String token = AESUtil.decrypt(config.getToken());

        ClickupContext.setToken(token);
        try {
            return clickupClient.getSpaces(config.getTeamId());
        } finally {
            ClickupContext.clear();
        }
    }

    public Object getLists(String spaceId) {
        ClickupConfig config = getConfig();
        String token = AESUtil.decrypt(config.getToken());

        ClickupContext.setToken(token);
        try {
            return clickupClient.getLists(spaceId);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object createSpace(String name) {
        ClickupConfig config = getConfig();
        String token = AESUtil.decrypt(config.getToken());

        ClickupContext.setToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);

        try {
            return clickupClient.createSpace(config.getTeamId(), body);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object createList(String spaceId, String name) {
        ClickupConfig config = getConfig();
        String token = AESUtil.decrypt(config.getToken());

        ClickupContext.setToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);

        try {
            return clickupClient.createList(spaceId, body);
        } finally {
            ClickupContext.clear();
        }
    }

    public void selectSpace(String spaceId) {
        ClickupConfig config = getConfig();
        config.setSpaceId(spaceId);
        repo.save(config);
    }

    public void selectList(String listId) {
        ClickupConfig config = getConfig();
        config.setListId(listId);
        repo.save(config);
    }
}