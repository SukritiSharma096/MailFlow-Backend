package com.mailProject.email.service;

import com.mailProject.email.entity.ClickupConfig;
import com.mailProject.email.exception.ClickupConfigNotFoundException;
import com.mailProject.email.exception.InvalidConfigException;
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
                .orElseThrow(() -> new ClickupConfigNotFoundException("ClickUp config not found"));
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
                    && c.getSpaceId() != null
                    && !c.getSpaceId().isBlank()
                    && c.getListId() != null
                    && !c.getListId().isBlank();
        } catch (ClickupConfigNotFoundException e) {
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

        if (config.getId() == null) {
            throw new InvalidConfigException("Config not initialized properly");
        }

        config.setSpaceId(spaceId);
        repo.save(config);
    }

    public void selectList(String listId) {
        ClickupConfig config = getConfig();

        if (config.getId() == null) {
            throw new InvalidConfigException("Config not initialized properly");
        }

        config.setListId(listId);
        repo.save(config);
    }

    public Object updateSpace(String spaceId, String name) {
        ClickupConfig config = getConfig();
        String token = AESUtil.decrypt(config.getToken());

        ClickupContext.setToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);

        try {
            return clickupClient.updateSpace(spaceId, body);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object deleteSpace(String spaceId) {
        ClickupConfig config = getConfig();
        String token = AESUtil.decrypt(config.getToken());

        ClickupContext.setToken(token);

        try {
            return clickupClient.deleteSpace(spaceId);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object updateList(String listId, String name) {
        ClickupConfig config = getConfig();
        String token = AESUtil.decrypt(config.getToken());

        ClickupContext.setToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);

        try {
            return clickupClient.updateList(listId, body);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object deleteList(String listId) {
        ClickupConfig config = getConfig();
        String token = AESUtil.decrypt(config.getToken());

        ClickupContext.setToken(token);

        try {
            return clickupClient.deleteList(listId);
        } finally {
            ClickupContext.clear();
        }
    }
}