package com.mailProject.email.controller;

import com.mailProject.email.dto.MailJobHistoryResponseDto;
import com.mailProject.email.service.MailJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@CrossOrigin("*")
public class MailJobController {

    private final MailJobService mailJobService;

    @PostMapping("/run")
    public ResponseEntity<String> runManually() {
        mailJobService.runJob("MANUAL");
        return ResponseEntity.ok("Job triggered successfully");
    }

    @GetMapping("/latest")
    public ResponseEntity<MailJobHistoryResponseDto> getLatest() {
        return ResponseEntity.ok(
                mailJobService.getLatest()
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<MailJobHistoryResponseDto>> getHistory() {
        return ResponseEntity.ok(
                mailJobService.getHistory()
        );
    }
}
