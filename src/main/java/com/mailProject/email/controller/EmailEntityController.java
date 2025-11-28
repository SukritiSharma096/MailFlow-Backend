package com.mailProject.email.controller;

import com.mailProject.email.dto.EmailEntityRequest;
import com.mailProject.email.dto.EmailResponse;
import com.mailProject.email.service.EmailEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/emails")
public class EmailEntityController {

    @Autowired
    private EmailEntityService emailService;

    @PostMapping
    public ResponseEntity<EmailResponse> save(@RequestBody EmailEntityRequest request) {
        return ResponseEntity.ok(emailService.saveEmail(request));
    }

    @GetMapping
    public ResponseEntity<List<EmailResponse>> getAll() {
        return ResponseEntity.ok(emailService.getAllEmails());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(emailService.getEmailById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailResponse> update(
            @PathVariable Long id,
            @RequestBody EmailEntityRequest request
    ) {
        return ResponseEntity.ok(emailService.updateEmail(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return ResponseEntity.ok(emailService.deleteEmail(id));
    }
}
