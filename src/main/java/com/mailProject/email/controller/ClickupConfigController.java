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

        service.saveOrUpdate(
                req.get("token"),
                req.get("listId")
        );

        return ResponseEntity.ok("Saved successfully");
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(
                Map.of("configured", service.isConfigured())
        );
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {

        ClickupConfig config = service.getConfig();

        return ResponseEntity.ok(
                Map.of(
                        "listId", config.getListId()
                )
        );
    }
}
