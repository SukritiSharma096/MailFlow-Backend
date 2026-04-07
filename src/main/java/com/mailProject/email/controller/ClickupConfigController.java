package com.mailProject.email.controller;

import com.mailProject.email.service.ClickupConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clickup")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ClickupConfigController {

    private final ClickupConfigService service;

    @PostMapping("/global-config")
    public ResponseEntity<?> saveGlobal(@RequestBody Map<String,String> req) {
        service.saveGlobalConfig(req.get("token"), req.get("teamId"));
        return ResponseEntity.ok("Global Config Saved");
    }

    @GetMapping("/spaces")
    public ResponseEntity<?> getSpaces() {
        return ResponseEntity.ok(service.getSpaces());
    }

    @PostMapping("/space")
    public ResponseEntity<?> createSpace(@RequestBody Map<String,String> req) {
        return ResponseEntity.ok(service.createSpace(req.get("name")));
    }

    @PutMapping("/space/{spaceId}")
    public ResponseEntity<?> updateSpace(
            @PathVariable String spaceId,
            @RequestBody Map<String,String> req) {

        return ResponseEntity.ok(
                service.updateSpace(spaceId, req.get("name"))
        );
    }

    @DeleteMapping("/space/{spaceId}")
    public ResponseEntity<?> deleteSpace(@PathVariable String spaceId) {
        return ResponseEntity.ok(service.deleteSpace(spaceId));
    }

    @GetMapping("/lists/{spaceId}")
    public ResponseEntity<?> getLists(@PathVariable String spaceId) {
        return ResponseEntity.ok(service.getLists(spaceId));
    }

    @PostMapping("/list")
    public ResponseEntity<?> createList(@RequestBody Map<String,String> req) {
        return ResponseEntity.ok(
                service.createList(req.get("spaceId"), req.get("name"))
        );
    }

    @PutMapping("/list/{listId}")
    public ResponseEntity<?> updateList(
            @PathVariable String listId,
            @RequestBody Map<String,String> req) {

        return ResponseEntity.ok(
                service.updateList(listId, req.get("name"))
        );
    }

    @DeleteMapping("/list/{listId}")
    public ResponseEntity<?> deleteList(@PathVariable String listId) {
        return ResponseEntity.ok(service.deleteList(listId));
    }

    @PostMapping("/account-config/{accountId}")
    public ResponseEntity<?> saveAccountConfig(
            @PathVariable Long accountId,
            @RequestBody Map<String,String> req
    ) {
        service.saveAccountConfig(accountId, req.get("spaceId"), req.get("listId"));
        return ResponseEntity.ok("Account Config Saved");
    }

    @GetMapping("/get-account-config/{accountId}")
    public ResponseEntity<?> getAccountConfig(@PathVariable Long accountId) {
        try {
            var mapping = service.getAccountConfig(accountId);
            Map<String, Object> res = new HashMap<>();
            res.put("spaceId", mapping.getSpaceId());
            res.put("listId", mapping.getListId());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("No config found");
        }
    }
}