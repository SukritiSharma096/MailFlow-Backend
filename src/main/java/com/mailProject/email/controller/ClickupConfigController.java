package com.mailProject.email.controller;

import com.mailProject.email.entity.ClickupConfig;
import com.mailProject.email.service.ClickupConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clickup")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ClickupConfigController {

    private final ClickupConfigService service;

    @PostMapping("/config")
    public ResponseEntity<?> saveConfig(@RequestBody Map<String, String> req) {

        service.save(
                req.get("token"),
                req.get("teamId")
        );

        return ResponseEntity.ok("Saved");
    }

    @GetMapping("/spaces")
    public ResponseEntity<?> getSpaces() {
        return ResponseEntity.ok(service.getSpaces());
    }

    @PostMapping("/space")
    public ResponseEntity<?> createSpace(@RequestBody Map<String, String> req) {
        return ResponseEntity.ok(service.createSpace(req.get("name")));
    }

    @PostMapping("/select-space")
    public ResponseEntity<?> selectSpace(@RequestBody Map<String, String> req) {
        service.selectSpace(req.get("spaceId"));
        return ResponseEntity.ok("Space Selected");
    }

    @GetMapping("/lists/{spaceId}")
    public ResponseEntity<?> getLists(@PathVariable String spaceId) {
        return ResponseEntity.ok(service.getLists(spaceId));
    }

    @PostMapping("/list")
    public ResponseEntity<?> createList(@RequestBody Map<String, String> req) {
        return ResponseEntity.ok(
                service.createList(req.get("spaceId"), req.get("name"))
        );
    }

    @PostMapping("/select-list")
    public ResponseEntity<?> selectList(@RequestBody Map<String, String> req) {
        service.selectList(req.get("listId"));
        return ResponseEntity.ok("List Selected");
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        ClickupConfig config = service.getConfig();

        return ResponseEntity.ok(
                Map.of(
                        "teamId", config.getTeamId(),
                        "spaceId", config.getSpaceId(),
                        "listId", config.getListId(),
                        "configured", true
                )
        );
    }
}