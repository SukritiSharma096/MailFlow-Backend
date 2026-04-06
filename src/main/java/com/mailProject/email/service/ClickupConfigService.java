package com.mailProject.email.service;

import com.mailProject.email.entity.ClickupAccountMapping;
import com.mailProject.email.entity.ClickupConfig;
import com.mailProject.email.entity.MultipleEmailAccounts;
import com.mailProject.email.exception.ClickupConfigNotFoundException;
import com.mailProject.email.feignInterface.ClickupClient;
import com.mailProject.email.repository.ClickupAccountMappingRepository;
import com.mailProject.email.repository.ClickupConfigRepository;
import com.mailProject.email.repository.MultipleEmailRepository;
import com.mailProject.email.security.AESUtil;
import com.mailProject.email.security.ClickupContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClickupConfigService {

    private final ClickupClient client;
    private final ClickupConfigRepository globalRepo;
    private final ClickupAccountMappingRepository mappingRepo;
    private final MultipleEmailRepository accountRepo;


    public void saveGlobalConfig(String token, String teamId) {
        ClickupConfig config = globalRepo.findTopByOrderByIdDesc()
                .orElse(new ClickupConfig());

        config.setToken(AESUtil.encrypt(token));
        config.setTeamId(teamId);

        globalRepo.save(config);
    }

    public ClickupConfig getGlobal() {
        return globalRepo.findTopByOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("Global config not found"));
    }

    public void saveAccountConfig(Long accountId, String spaceId, String listId) {

        MultipleEmailAccounts acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        ClickupAccountMapping mapping = mappingRepo.findByAccountId(accountId)
                .orElse(new ClickupAccountMapping());

        mapping.setAccount(acc);
        mapping.setSpaceId(spaceId);
        mapping.setListId(listId);

        mappingRepo.save(mapping);
    }

    public ClickupAccountMapping getAccountConfig(Long accountId) {
        return mappingRepo.findByAccountId(accountId)
                .orElseThrow(() -> new ClickupConfigNotFoundException("Mapping not found"));
    }

    public boolean isConfigured(Long accountId) {
        try {
            ClickupConfig global = getGlobal();
            ClickupAccountMapping mapping = getAccountConfig(accountId);

            return global.getToken() != null &&
                    global.getTeamId() != null &&
                    mapping.getSpaceId() != null &&
                    mapping.getListId() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public Object getSpaces() {
        ClickupConfig config = getGlobal();

        String token = AESUtil.decrypt(config.getToken());
        ClickupContext.setToken(token);

        try {
            return client.getSpaces(config.getTeamId());
        } finally {
            ClickupContext.clear();
        }
    }

    public Object createSpace(String name) {
        ClickupConfig config = getGlobal();

        String token = AESUtil.decrypt(config.getToken());
        ClickupContext.setToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);

        try {
            return client.createSpace(config.getTeamId(), body);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object updateSpace(String spaceId, String name) {
        ClickupConfig config = getGlobal();

        String token = AESUtil.decrypt(config.getToken());
        ClickupContext.setToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);

        try {
            return client.updateSpace(spaceId, body);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object deleteSpace(String spaceId) {
        ClickupConfig config = getGlobal();

        String token = AESUtil.decrypt(config.getToken());
        ClickupContext.setToken(token);

        try {
            return client.deleteSpace(spaceId);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object getLists(String spaceId) {
        ClickupConfig config = getGlobal();

        String token = AESUtil.decrypt(config.getToken());
        ClickupContext.setToken(token);

        try {
            return client.getLists(spaceId);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object createList(String spaceId, String name) {
        ClickupConfig config = getGlobal();

        String token = AESUtil.decrypt(config.getToken());
        ClickupContext.setToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);

        try {
            return client.createList(spaceId, body);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object updateList(String listId, String name) {
        ClickupConfig config = getGlobal();

        String token = AESUtil.decrypt(config.getToken());
        ClickupContext.setToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);

        try {
            return client.updateList(listId, body);
        } finally {
            ClickupContext.clear();
        }
    }

    public Object deleteList(String listId) {
        ClickupConfig config = getGlobal();

        String token = AESUtil.decrypt(config.getToken());
        ClickupContext.setToken(token);

        try {
            return client.deleteList(listId);
        } finally {
            ClickupContext.clear();
        }
    }
}