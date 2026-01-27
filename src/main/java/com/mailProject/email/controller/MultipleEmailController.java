package com.mailProject.email.controller;


import com.mailProject.email.dto.MultipleEmailRequest;
import com.mailProject.email.dto.MultipleEmailResponse;
import com.mailProject.email.dto.SendEmailRequest;
import com.mailProject.email.service.MultipleEmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/accounts")
public class MultipleEmailController {

    private final MultipleEmailService service;

    public MultipleEmailController(MultipleEmailService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ResponseEntity<MultipleEmailResponse> create(@RequestBody MultipleEmailRequest req) {
        return ResponseEntity.ok(service.createAccount(req));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<MultipleEmailResponse>> list() {
        return ResponseEntity.ok(service.listAccounts());
    }


    @GetMapping("/{id}")
    public ResponseEntity<MultipleEmailResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAccount(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MultipleEmailResponse> update(@PathVariable Long id, @RequestBody MultipleEmailRequest req) {
        return ResponseEntity.ok(service.updateAccount(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    // ===============================================================================================================


    @PostMapping("/{id}/send")
    public ResponseEntity<String> send(@PathVariable Long id, @RequestBody SendEmailRequest req) {
        try {
            service.sendEmail(id, req);
            return ResponseEntity.ok("Email sent");
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Send failed: " + ex.getMessage());
        }
    }


    @PostMapping(value = "/sendWithAttachments/{accountId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendEmailWithAttachments(
            @PathVariable Long accountId,
            @ModelAttribute SendEmailRequest request) {

        try {
            service.sendEmailWithAttachments(accountId, request);
            return ResponseEntity.ok("Email sent successfully with multiple attachments!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed: " + e.getMessage());
        }
    }

    @GetMapping("/inbox/{accountId}")
    public ResponseEntity<?> getInbox(@PathVariable Long accountId) {
        try {
            return ResponseEntity.ok(service.fetchInbox(accountId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/move/google/{id}")
    public ResponseEntity<?> moveGoogle(@PathVariable Long id) {
        service.moveGoogleMails(id);
        return ResponseEntity.ok("Google mails moved!");
    }

    @PostMapping("/move/youtube/{id}")
    public ResponseEntity<?> moveYouTube(@PathVariable Long id) {
        service.moveYouTubeMails(id);
        return ResponseEntity.ok("YouTube mails moved!");
    }

    @PostMapping("/move/netflix/{id}")
    public ResponseEntity<?> moveNetflix(@PathVariable Long id) {
        service.moveNetflixMails(id);
        return ResponseEntity.ok("Netflix mails moved!");
    }

    @PostMapping("/move/pokemongo/{id}")
    public ResponseEntity<?> movePokemonGo(@PathVariable Long id) {
        service.movePokemonGoMails(id);
        return ResponseEntity.ok("PokemonGo mails moved!");
    }

    @PostMapping("/move/hdfc/{id}")
    public ResponseEntity<?> moveHDFCBankMails(@PathVariable Long id){
        service.moveHDFCBankMails(id);
        return ResponseEntity.ok("HDFC bank mails moved");
    }

    @PostMapping("/move/linkedIn/{id}")
    public ResponseEntity<?> moveLinkedInMails(@PathVariable Long id){
        service.moveLinkedInMails(id);
        return  ResponseEntity.ok("Linkedin mails moved");
    }

    @DeleteMapping("/{accountId}/delete/{emailId}")
    public ResponseEntity<String> deleteEmail(
            @PathVariable Long accountId,
            @PathVariable Long emailId) {

        service.deleteEmailFromDb(accountId, emailId);
        return ResponseEntity.ok(
                "Email with ID " + emailId + " deleted successfully from account " + accountId
        );
    }

}
